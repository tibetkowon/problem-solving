package com.problemsolving.api.chapter.service;

import com.problemsolving.api.chapter.dto.ChapterResponse;
import com.problemsolving.core.repository.ChapterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChapterService {

    private final ChapterRepository chapterRepository;

    public List<ChapterResponse> findAll() {
        return chapterRepository.findAll().stream()
                .map(ChapterResponse::new)
                .toList();
    }
}
