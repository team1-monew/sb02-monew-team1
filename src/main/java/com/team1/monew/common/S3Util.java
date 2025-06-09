package com.team1.monew.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.InputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3Util {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public boolean isBackupExists(String key) {
        try {
            log.info("🔍 S3에서 백업 파일 존재 여부 확인 중: key={}", key);

            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.headObject(headObjectRequest);

            log.info("✅ 백업 파일 존재: key={}", key);
            return true;
        } catch (S3Exception e) {
            if (e.awsErrorDetails().errorCode().equals("NoSuchKey")) {
                log.info("❌ 백업 파일 없음: key={}", key);
                return false;
            } else {
                log.error("❌ S3 파일 존재 여부 확인 중 오류 발생: key={}, message={}", key, e.getMessage());
                throw e;
            }
        }
    }

    public void upload(String key, InputStream inputStream, long length, String contentType) {
        try {
            log.info("📤 S3 업로드 시작: key={}, length={}, contentType={}", key, length, contentType);

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(inputStream, length));

            log.info("✅ S3 업로드 성공: key={}", key);
        } catch (Exception e) {
            log.error("❌ S3 업로드 실패: key={}, message={}", key, e.getMessage(), e);
        }
    }

    public byte[] download(String key) {
        log.info("📥 S3에서 파일 다운로드 시작: key={}", key);

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        ResponseBytes<GetObjectResponse> objectBytes = s3Client.getObjectAsBytes(getObjectRequest);

        log.info("✅ S3에서 파일 다운로드 완료: key={}", key);
        return objectBytes.asByteArray();
    }

    public void delete(String key) {
        try {
            log.info("🗑️ S3에서 파일 삭제 시작: key={}", key);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("✅ S3에서 파일 삭제 완료: key={}", key);
        } catch (Exception e) {
            log.error("❌ S3에서 파일 삭제 실패: key={}, message={}", key, e.getMessage(), e);
        }
    }
}

