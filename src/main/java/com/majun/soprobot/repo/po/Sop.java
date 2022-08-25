package com.majun.soprobot.repo.po;

import org.springframework.data.annotation.Id;

public record Sop(@Id Integer id, String chatId, String docToken, String docUrl, String title, String description) {
}
