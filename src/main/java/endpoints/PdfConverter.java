package endpoints;

import com.google.protobuf.Timestamp;
import io.desertrat.keepsake.protocols.CommonResponseMetaOuterClass;
import io.desertrat.keepsake.protocols.pdfconverter.ConvertPdfToJpegRequest;
import io.desertrat.keepsake.protocols.pdfconverter.ConvertPdfToJpegResponse;
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

        this.pdfConversionProcessor.process(pdfToJpegRequest);


        var response = ConvertPdfToJpegResponse.newBuilder().setMeta(
                CommonResponseMetaOuterClass
                        .CommonResponseMeta
                        .newBuilder()
                        .setCorrelationId(pdfToJpegRequest.getCorrelationId())
                        .setMessage("PDF processed")
                        .setTimestamp(Timestamp.getDefaultInstance())
                        .build()
        ).build();
        return Uni.createFrom().item(response);
    }


}
