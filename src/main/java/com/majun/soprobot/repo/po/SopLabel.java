package com.majun.soprobot.repo.po;

import org.springframework.data.annotation.Id;

public record SopLabel(@Id Integer id, Integer sopId, String name) {
}
