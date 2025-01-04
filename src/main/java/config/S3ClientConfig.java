package config;

import jakarta.enterprise.context.Dependent;
import jakarta.enterprise.inject.Produces;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.transfer.s3.S3TransferManager;

@Dependent
public class S3ClientConfig {

    @Produces
    public S3TransferManager buildTransferManager() {
        var s3Client = S3AsyncClient.builder()
                .credentialsProvider(DefaultCredentialsProvider.create())
                .multipartEnabled(true)
                .region(Region.EU_NORTH_1)
                .build();
        return S3TransferManager.builder().
                s3Client(s3Client).build();
    }
}
