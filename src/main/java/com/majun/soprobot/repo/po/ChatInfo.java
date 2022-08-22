package com.majun.soprobot.repo.po;

import org.springframework.data.annotation.Id;

public record ChatInfo(@Id Integer id, String chatId, String folderToken, String folderUrl) {
}
