//package com.majun.soprobot.lark.api;
//
//import org.junit.jupiter.api.MethodOrderer;
//import org.junit.jupiter.api.Order;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.TestMethodOrder;
//
//import java.util.List;
//import java.util.Optional;
//
//@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
//class LarkApiTest {
//
//    LarkApi larkApi = new LarkApi("cli_a270a6db85fad00b", "LZxrWWcHKavl6xyGLN0behga8y0neDku");
//
//    private static String accessToken;
//    private static String rootFolderToken;
//    private static List<String> filesToken;
//    private static LarkApi.FolderCreateResp folderCreateResp;
//
//    @Test
//    @Order(1)
//    void tenantAccessToken() {
//        Optional<String> optional = larkApi.tenantAccessToken().blockOptional();
//        assert optional.isPresent();
//        accessToken = optional.get();
//    }
//
//    @Test
//    @Order(2)
//    void rootFolderToken() {
//        Optional<String> optional = larkApi.rootFolderToken(accessToken).blockOptional();
//        assert optional.isPresent();
//        rootFolderToken = optional.get();
//    }
//
//    @Test
//    void getFilesToken() {
//        Optional<List<String>> optional = larkApi.getFilesToken(accessToken, rootFolderToken).collectList().blockOptional();
//        assert optional.isPresent();
//        filesToken = optional.get();
//    }
//
//    @Test
//    void createFolder() {
//        Optional<LarkApi.FolderCreateResp> optional = larkApi.createFolder(accessToken, rootFolderToken, "unit_test").blockOptional();
//        assert optional.isPresent();
//        folderCreateResp = optional.get();
//    }
//
//    @Test
//    void createFile() {
//    }
//
//    @Test
//    void createPermissionMember() {
//    }
//
//    @Test
//    void permitted() {
//    }
//
//    @Test
//    void getAllBlock() {
//    }
//
//    @Test
//    void getBlock() {
//    }
//
//    @Test
//    void sendMessage() {
//    }
//}