package gamza.project.gamzaweb.dctutil;

import gamza.project.gamzaweb.error.ErrorCode;
import gamza.project.gamzaweb.error.requestError.BadRequestException;

import javax.swing.filechooser.FileSystemView;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FileController {

    public static String defaultPath = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();

    // return file path
    public static String saveFile(InputStream inputStream, String projectId, String fileName) {
        // 저장할 디렉터리 경로를 구성
        String directoryPath = defaultPath + File.separator + projectId;
        File directory = new File(directoryPath);

        boolean mkdirResult = false;
        // 디렉터리가 없으면 생성
        if (!directory.exists()) {
            mkdirResult = directory.mkdirs();
        }

        if (!mkdirResult) {
            throw new RuntimeException("Unable to create directory " + directoryPath);
        }

        String filePath = directoryPath + File.separator + fileName + ".zip";
//        String filePath = directoryPath;
        File file = new File(filePath);

        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return directoryPath; // 저장된 파일 경로 반환
    }

    public static void deleteFileInRoot(String filePath) {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("bash", "-c", "sudo rm -rf " + filePath);
            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("삭제 성공: " + filePath);
            } else {
                System.err.println("삭제 실패, 종료 코드: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new BadRequestException("Root 경로 폴더가 삭제되지 않았습니다.", ErrorCode.INTERNAL_SERVER_EXCEPTION);
        }
    }

    public static void deleteFile(String filePath) {
        File file = new File(filePath);
    // sudo rm -rf /root/test0414
        if (file.exists()) {
            if (file.delete()) {
                System.out.println("파일 삭제 성공: " + filePath);
            } else {
                System.err.println("파일 삭제 실패: " + filePath);
            }
        } else {
            System.out.println("삭제할 파일이 존재하지 않음: " + filePath);
        }
    }


    public static File unzip(String zipFilePath) {
        File zipFile = new File(zipFilePath);
        String destDirectory = zipFile.getParent(); // zip 파일과 같은 폴더에 압축 해제
        File destDir = new File(destDirectory);

        if (!destDir.exists()) {
            if (!destDir.mkdirs()) {
                System.err.println("Failed to create destination directory.");
                return null;
            }
        }

        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile), Charset.forName("EUC-KR"))) {
            ZipEntry entry = zipIn.getNextEntry();

            // 각 ZipEntry를 반복해서 압축 해제
            while (entry != null) {
                String filePath = destDirectory + File.separator + entry.getName();
                if (!entry.isDirectory()) {
                    // 파일인 경우 파일을 저장
                    extractFile(zipIn, filePath);
                } else {
                    // 디렉토리인 경우 디렉토리 생성
                    File dir = new File(filePath);
                    if (!dir.exists()) {
                        if (!dir.mkdirs()) {
                            System.err.println("Failed to create directory: " + filePath);
                            return null;
                        }
                    }
                }
                zipIn.closeEntry();
                entry = zipIn.getNextEntry();

            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
//        return true; // 압축 해제 성공 시 true 반환
        return destDir; // 압축 해제 성공 시 폴더 경로 반환
    }


//    public static boolean unzip(String zipFilePath) {
//        File zipFile = new File(zipFilePath);
//        String destDirectory = zipFile.getParent(); // zip 파일과 같은 폴더에 압축 해제
//        File destDir = new File(destDirectory);
//
//        boolean result = false;
//        if (!destDir.exists()) {
//            result = destDir.mkdirs();
//        }
//        System.out.println(result);
//
//        if (!result) {
//            return false;
//        }
//
//        try (ZipInputStream zipIn = new ZipInputStream(new FileInputStream(zipFile))) {
//            ZipEntry entry = zipIn.getNextEntry();
//            // 각 ZipEntry를 반복해서 압축을 해제
//            while (entry != null) {
//                if (!result) {
//                    return false;
//                }
//                //폴더 내에 새로운 폴더 만들거면 위의 코드
////                String filePath = destDirectory + File.separator + entry.getName();
//                //만약에 같은 폴더에 할거면 아래 코드
//                String filePath = destDirectory;
//                if (!entry.isDirectory()) {
//                    // 파일인 경우 파일을 저장
//                    extractFile(zipIn, filePath);
//                } else {
//                    // 디렉토리인 경우 디렉토리 생성
//                    File dir = new File(filePath);
//                    result = dir.mkdirs();
//                }
//                zipIn.closeEntry();
//                entry = zipIn.getNextEntry();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }

    private static void extractFile(ZipInputStream zipIn, String filePath) throws IOException {
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(filePath));
        byte[] bytesIn = new byte[4096];
        int read;
        while ((read = zipIn.read(bytesIn)) != -1) {
            bos.write(bytesIn, 0, read);
        }
        bos.close();
    }
}
