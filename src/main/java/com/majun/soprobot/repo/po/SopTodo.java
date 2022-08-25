package com.majun.soprobot.repo.po;

import org.springframework.data.annotation.Id;

public record SopTodo(@Id Integer id, Integer sopId, String docToken, String description) {
}
