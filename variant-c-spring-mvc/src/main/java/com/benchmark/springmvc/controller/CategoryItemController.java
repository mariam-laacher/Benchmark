package com.benchmark.springmvc.controller;

import com.benchmark.common.entity.Item;
import com.benchmark.springmvc.repository.ItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/categories/{categoryId}/items")
public class CategoryItemController {
    private final ItemRepository itemRepository;
    private static final boolean USE_JOIN_FETCH = Boolean.parseBoolean(System.getenv().getOrDefault("USE_JOIN_FETCH", "false"));

    public CategoryItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Item>> getItemsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Item> result = USE_JOIN_FETCH
                ? itemRepository.findByCategoryIdWithJoinFetch(categoryId, pageable)
                : itemRepository.findByCategoryId(categoryId, pageable);
        return ResponseEntity.ok(result);
    }
}

