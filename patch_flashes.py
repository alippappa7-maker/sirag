import re

with open("app/src/main/java/com/siraj/app/features/flashes/presentation/FlashesScreen.kt", "r") as f:
    content = f.read()

content = content.replace("VerticalPager(\n                state = pagerState,\n                modifier = Modifier.fillMaxSize()\n            ) { page ->", "VerticalPager(\n                state = pagerState,\n                modifier = Modifier.fillMaxSize(),\n                key = { state.flashes[it].id }\n            ) { page ->")

with open("app/src/main/java/com/siraj/app/features/flashes/presentation/FlashesScreen.kt", "w") as f:
    f.write(content)
