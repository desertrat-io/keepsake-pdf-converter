package endpoints;

import com.google.protobuf.Timestamp;
import io.desertrat.keepsake.protocols.CommonResponseMetaOuterClass;
import io.desertrat.keepsake.protocols.pdfconverter.ConvertPdfToJpegRequest;
import io.desertrat.keepsake.protocols.pdfconverter.ConvertPdfToJpegResponse;
import io.desertrat.keepsake.protocols.pdfconverter.FilePointers;
import io.desertrat.keepsake.protocols.pdfconverter.KeepsakePdfConverter;
import io.quarkus.grpc.GrpcService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import processors.PdfConversionProcessor;

@GrpcService
public class PdfConverter implements KeepsakePdfConverter {

    private final PdfConversionProcessor pdfConversionProcessor;

    @Inject
    public PdfConverter(PdfConversionProcessor pdfConversionService) {
        this.pdfConversionProcessor = pdfConversionService;
    }

    public Uni<ConvertPdfToJpegResponse> convertToPdf(ConvertPdfToJpegRequest pdfToJpegRequest) {
        try {
            var paths = this.pdfConversionProcessor.process(pdfToJpegRequest);
            var response = ConvertPdfToJpegResponse.newBuilder().setMeta(
                    CommonResponseMetaOuterClass
                            .CommonResponseMeta
                            .newBuilder()
                            .setCorrelationId(pdfToJpegRequest.getCorrelationId())
                            .setMessage("PDF processed")
                            .setTimestamp(Timestamp.getDefaultInstance())
                            .build()
            );

            paths.forEach(path -> {
                var filePathMessage = FilePointers.newBuilder()
                        .setFileFinalLocation(path.getFileFinalLocation())
                        .setFileName(path.getFileName())
                        .setFileMime("image/jpeg")
                        .setPageFileSize(path.getPageFileSize())
                        .setPageNum(path.getPageNum())
                        .build();
                response.addFiles(filePathMessage);
            });
            return Uni.createFrom().item(response.build());
        } catch (Exception e) {

            return Uni.createFrom().item(ConvertPdfToJpegResponse.newBuilder().setMeta(
                            CommonResponseMetaOuterClass
                                    .CommonResponseMeta
                                    .newBuilder()
                                    .setCorrelationId(pdfToJpegRequest.getCorrelationId())
                                    .setMessage(e.getMessage())
                                    .setTimestamp(Timestamp.getDefaultInstance())
                                    .build()
                    )
                    .build());
        }


    }


}
