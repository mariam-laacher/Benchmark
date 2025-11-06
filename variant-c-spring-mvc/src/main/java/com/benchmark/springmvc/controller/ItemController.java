package com.benchmark.springmvc.controller;

import com.benchmark.common.entity.Item;
import com.benchmark.springmvc.repository.ItemRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
public class ItemController {
    private final ItemRepository itemRepository;
    private static final boolean USE_JOIN_FETCH = Boolean.parseBoolean(System.getenv().getOrDefault("USE_JOIN_FETCH", "false"));

    public ItemController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping
    public ResponseEntity<Page<Item>> getAll(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Item> result;
        if (categoryId != null) {
            result = USE_JOIN_FETCH
                    ? itemRepository.findByCategoryIdWithJoinFetch(categoryId, pageable)
                    : itemRepository.findByCategoryId(categoryId, pageable);
        } else {
            result = USE_JOIN_FETCH
                    ? itemRepository.findAllWithJoinFetch(pageable)
                    : itemRepository.findAll(pageable);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getById(@PathVariable Long id) {
        return itemRepository.findById(id)
                .map(item -> {
                    if (USE_JOIN_FETCH && item.getCategory() != null) {
                        item.getCategory().getName();
                    }
                    return ResponseEntity.ok(item);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Item> create(@Valid @RequestBody Item item) {
        Item saved = itemRepository.save(item);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> update(@PathVariable Long id, @Valid @RequestBody Item item) {
        return itemRepository.findById(id)
                .map(existing -> {
                    item.setId(id);
                    Item updated = itemRepository.save(item);
                    return ResponseEntity.ok(updated);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!itemRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        itemRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

