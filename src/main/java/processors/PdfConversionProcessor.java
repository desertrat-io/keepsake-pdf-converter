package processors;


import entities.PdfDocumentEntity;
import events.NewDocumentToProcess;
import io.desertrat.keepsake.protocols.S3DataStoreOuterClass;
import io.desertrat.keepsake.protocols.pdfconverter.ConvertPdfToJpegRequest;
import io.quarkus.logging.Log;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@ApplicationScoped
public class PdfConversionProcessor implements RequestProcessor<ConvertPdfToJpegRequest> {
    private final NewDocumentToProcess newDocumentToProcess;
    private ConvertPdfToJpegRequest pdfToJpegRequest;

    @Inject
    public PdfConversionProcessor(NewDocumentToProcess newDocumentToProcess) {
        this.newDocumentToProcess = newDocumentToProcess;
    }

    public void process(ConvertPdfToJpegRequest pdfToJpegRequest) {
        this.pdfToJpegRequest = pdfToJpegRequest;
        var s3Client = buildClient(pdfToJpegRequest.getS3DataStore().getRegion());
        var s3Transfer = buildTransferManager(s3Client);
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
            var entity = new PdfDocumentEntity(pdfToJpegRequest.getS3DataStore().getFileKey(), pdfToJpegRequest.getFileName());

            newDocumentToProcess.saveToEventStore(entity, pdfToJpegRequest.getS3DataStore().getFileKey());
        } catch (Exception e) {
            Log.error(e);
        }

    }

    private S3AsyncClient buildClient(String region) {
        return S3AsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .multipartEnabled(true)
                .region(Region.of(region))
                .build();
    }

    private S3TransferManager buildTransferManager(S3AsyncClient s3Client) {
        return S3TransferManager.builder().
                s3Client(s3Client).build();
    }

    private DownloadFileRequest buildDownloadObjectRequest(S3DataStoreOuterClass.S3DataStore dataStore) throws IOException {
        Files.createDirectories(Path.of("/tmp/", dataStore.getFilePath()));
        return DownloadFileRequest.builder()
                .getObjectRequest(b -> b.bucket(dataStore.getBucketName())
                        .key(dataStore.getFileKey()))
                .destination(Path.of("/tmp/", dataStore.getFileKey()))
                .build();
    }
}
