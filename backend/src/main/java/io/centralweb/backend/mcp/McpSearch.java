package io.centralweb.backend.mcp;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.util.List;

public final class McpSearch {
    private McpSearch() {}

    static PageRequest page(int page) {
        if (page < 0 || page > 100) {
            throw new IllegalArgumentException("A página deve estar entre 0 e 100.");
        }
        return PageRequest.of(page, 5);
    }

    static String term(String term) {
        if (term == null || term.isBlank() || term.length() > 200) {
            throw new IllegalArgumentException("Informe um termo de busca entre 1 e 200 caracteres.");
        }
        return term.trim();
    }

    static <T> Results<T> results(Page<T> page) {
        return new Results<>(page.getContent(), page.getNumber(), page.hasNext());
    }

    public record Results<T>(List<T> items, int page, boolean hasNext) {}
}
