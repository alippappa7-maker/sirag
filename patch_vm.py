import re

with open('app/src/main/java/com/siraj/app/features/subscription/presentation/SubscriptionViewModel.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Add transactions to SubscriptionState
content = content.replace(
    "val availablePlans: List<Plan> = emptyList(),",
    "val availablePlans: List<Plan> = emptyList(),\n    val transactions: List<com.siraj.app.domain.models.subscription.CreditTransaction> = emptyList(),"
)

# Add load transactions
load_block = """            launch {
                repository.getAvailablePlans().collectLatest { plans ->
                    _state.update { it.copy(availablePlans = plans) }
                }
            }"""
new_load_block = load_block + """
            launch {
                repository.getCreditTransactions(userId, null, 50).collectLatest { txs ->
                    _state.update { it.copy(transactions = txs) }
                }
            }"""
content = content.replace(load_block, new_load_block)

with open('app/src/main/java/com/siraj/app/features/subscription/presentation/SubscriptionViewModel.kt', 'w', encoding='utf-8') as f:
    f.write(content)
