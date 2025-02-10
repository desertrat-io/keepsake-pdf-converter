package processors;


import entities.PageEntity;
import entities.PdfDocumentEntity;
import events.DocumentSplit;
import events.NewDocumentToProcess;
import io.desertrat.keepsake.protocols.S3DataStoreOuterClass;
import io.desertrat.keepsake.protocols.pdfconverter.ConvertPdfToJpegRequest;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.eclipse.microprofile.config.Config;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.CompletedDirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.DirectoryUpload;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;
import software.amazon.awssdk.transfer.s3.model.UploadDirectoryRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class PdfConversionProcessor implements RequestProcessor<ConvertPdfToJpegRequest, List<PageEntity>> {
    private final NewDocumentToProcess newDocumentToProcess;
    private final DocumentSplit documentSplit;
    private final S3TransferManager s3Transfer;
    private final int pageDpi;
    private final String baseLocalDirectory;
    private ConvertPdfToJpegRequest pdfToJpegRequest;

    @Inject
    public PdfConversionProcessor(NewDocumentToProcess newDocumentToProcess, DocumentSplit documentSplit, S3TransferManager s3Transfer, Config config) {
        this.newDocumentToProcess = newDocumentToProcess;
        this.documentSplit = documentSplit;
        this.s3Transfer = s3Transfer;
        this.pageDpi = Integer.parseInt(config.getConfigValue("keepsake.pdf.page.dpi").getValue());
        this.baseLocalDirectory = config.getConfigValue("keepsake.pdf.base.local").getValue();
    }

    public List<PageEntity> process(ConvertPdfToJpegRequest pdfToJpegRequest) throws ExecutionException, InterruptedException, IOException {
        this.pdfToJpegRequest = pdfToJpegRequest;
        Log.info("Trying to download " + pdfToJpegRequest.getFileLocator());
        try {
            Log.info("build the request");
            var downloadFileRequest = buildDownloadObjectRequest(pdfToJpegRequest.getS3DataStore());
            Log.info("do the download");
            var downloadedFile = s3Transfer.downloadFile(downloadFileRequest);

            Log.info("grabbed a filename:" + downloadedFile.progress().toString());

            Log.info("then resolve");

            var completedFileDownloadRequest = downloadedFile.completionFuture().join();
            Log.info(completedFileDownloadRequest.toString());
            Log.info("Grabbed {" + completedFileDownloadRequest.response().contentLength() + "} bytes");
            var fileKey = pdfToJpegRequest.getS3DataStore().getFileKey();
            var entity = new PdfDocumentEntity();
            entity.setFileKey(fileKey);
            entity.setFileName(pdfToJpegRequest.getS3DataStore().getFileName());
            var newPagePaths = splitDocument(baseLocalDirectory, fileKey, pdfToJpegRequest.getS3DataStore().getFileName());
            entity.setNumPages(newPagePaths.size());
            newDocumentToProcess.saveToEventStore(entity, fileKey);
            var pathWithoutFile = fileKey.substring(0, fileKey.indexOf(pdfToJpegRequest.getS3DataStore().getFileName()));
            var directoryUpload = buildDirectoryUploadRequest(pdfToJpegRequest.getS3DataStore(), baseLocalDirectory + pathWithoutFile);
            CompletedDirectoryUpload completedDirectoryUpload = directoryUpload.completionFuture().join();
            completedDirectoryUpload.failedTransfers().forEach(failedTransfer -> Log.warn(failedTransfer.toString() + " failed to upload"));
            return newPagePaths;
        } catch (InterruptedException | ExecutionException | IOException e) {
            Log.error(e);
            throw e;
        }

    }


    private DownloadFileRequest buildDownloadObjectRequest(S3DataStoreOuterClass.S3DataStore dataStore) throws IOException {
        Files.createDirectories(Path.of(baseLocalDirectory, dataStore.getFilePath()));
        return DownloadFileRequest.builder()
                .getObjectRequest(b -> b.bucket(dataStore.getBucketName())
                        .key(dataStore.getFileKey()))
                .destination(Path.of(baseLocalDirectory, dataStore.getFileKey()))
                .build();
    }

    private DirectoryUpload buildDirectoryUploadRequest(S3DataStoreOuterClass.S3DataStore dataStore, String directoryToUpload) throws IOException {
        return s3Transfer.uploadDirectory(UploadDirectoryRequest.builder()
                .source(Paths.get(directoryToUpload))
                .s3Prefix(dataStore.getFilePath())
                .bucket(dataStore.getBucketName())
                .build());
    }

    private List<PageEntity> splitDocument(String pdfBasePath, String pdfFileKey, String pdfFileName) {

        try {
            Log.info("Trying to split: " + pdfFileKey);
            var document = Loader.loadPDF(new File(baseLocalDirectory + pdfFileKey));
            var renderer = new PDFRenderer(document);
            Log.info("PDF Loaded, grabbing pages");
            var pages = document.getPages();
            var fileNameWithoutExtension = pdfFileName.substring(0, pdfFileName.indexOf("."));
            var pathWithoutFile = pdfFileKey.substring(0, pdfFileKey.indexOf(pdfFileName));
            Log.info("Pages retrieved, now splitting");
            List<PageEntity> pageEntities = new ArrayList<>();
            for (PDPage page : pages) {
                var fileName = fileNameWithoutExtension + "_page_" + pages.indexOf(page) + ".jpg";
                Log.info("splitting into " + fileName);
                var bufferedImage = renderer.renderImageWithDPI(pages.indexOf(page), pageDpi, ImageType.RGB);
                ImageIOUtil.writeImage(bufferedImage, baseLocalDirectory + pathWithoutFile + '/' + fileName, pageDpi);
                var pageEntity = new PageEntity();
                pageEntity.setFileName(fileName);
                pageEntity.setFileFinalLocation(baseLocalDirectory + pathWithoutFile + '/' + fileName);
                pageEntity.setPageNum(pages.indexOf(page) + 1);
                pageEntity.setPageFileSize(0);
                pageEntity.setContainingPath(baseLocalDirectory + pathWithoutFile);
                pageEntities.add(pageEntity);
                Log.info("Page added");
                documentSplit.saveToEventStore(pageEntity, pdfFileKey);
            }
            document.close();
            return pageEntities;
        } catch (IOException | ExecutionException | InterruptedException e) {
            Log.error(e);
            Thread.currentThread().interrupt();
            return List.of();
        }
    }
}
