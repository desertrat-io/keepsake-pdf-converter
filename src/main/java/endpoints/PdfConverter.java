package endpoints;

import com.google.protobuf.Timestamp;
import io.desertrat.keepsake.protocols.pdfconverter.ConvertPdfToJpegRequest;
import io.desertrat.keepsake.protocols.pdfconverter.ConvertPdfToJpegResponse;
import io.desertrat.keepsake.protocols.pdfconverter.KeepsakePdfConverter;
import io.quarkus.grpc.GrpcService;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import keepsake.common.CommonResponseMetaOuterClass;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest;

@GrpcService

public class PdfConverter implements KeepsakePdfConverter {

    @ConfigProperty(name = "eventstore.db.uri")
    String eventStoreUri;

    @ConfigProperty(name = "keepsake.test.pdf")
    String testPdfPath;

    @ConfigProperty(name = "keepsake.primary.bucket")
    String keepsakePrimaryBucket;


    public Uni<ConvertPdfToJpegResponse> convertToPdf(ConvertPdfToJpegRequest thing) {
        Log.info(thing.toString());
        var s3Client = S3AsyncClient.builder()
                .multipartEnabled(true)
                .credentialsProvider(EnvironmentVariableCredentialsProvider.create())
                .region(Region.EU_NORTH_1)
                .build();
        var s3Transfer = S3TransferManager.builder().
                s3Client(s3Client).build();
        var downloadFileRequest = DownloadFileRequest.builder()
                .getObjectRequest(b -> b.bucket(keepsakePrimaryBucket)
                        .key(testPdfPath))
                .build();
        var downloadedFile = s3Transfer.downloadFile(downloadFileRequest);
        var completedFileDownloadRequest = downloadedFile.completionFuture().join();
        Log.info("Grabbed {" + completedFileDownloadRequest.response().contentLength() + "} bytes");
        /*var settings = EventStoreDBConnectionString.parseOrThrow(eventStoreUri);
        var client = EventStoreDBClient.create(settings);*/

        var response = ConvertPdfToJpegResponse.newBuilder().setMeta(
                CommonResponseMetaOuterClass.CommonResponseMeta.newBuilder().setCorrelationId(thing.getCorrelationId()).setMessage(testPdfPath).setTimestamp(Timestamp.getDefaultInstance()).build()
        ).build();
        return Uni.createFrom().item(response);
    }
}
