import sys
with open("app/src/test/java/com/siraj/app/features/subscription/presentation/SubscriptionViewModelTest.kt", "r") as f:
    content = f.read()

content = content.replace('CreditBalance("test", "user1", 100, 50, 0, 150)', 'CreditBalance("test", "user1", 100, 50, 0, 150L)')
content = content.replace("viewModel.state.value.balance?.regularBalance", "viewModel.state.value.balance?.availableCredits")
content = content.replace("viewModel.state.value.balance?.bonusBalance", "viewModel.state.value.balance?.totalPurchased")
content = content.replace("assertEquals(150L, viewModel.state.value.balance?.totalBalance)", "assertEquals(0, viewModel.state.value.balance?.totalUsed)")
content = content.replace("100L", "100")
content = content.replace("50L", "50")

with open("app/src/test/java/com/siraj/app/features/subscription/presentation/SubscriptionViewModelTest.kt", "w") as f:
    f.write(content)
