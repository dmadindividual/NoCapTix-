package topg.Event_Platform.dto;

import lombok.Builder;

@Builder
public record ResolvedBankAccountDto(
        boolean success,
        String accountName,
        String message
) {}
