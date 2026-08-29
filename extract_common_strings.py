import os
import re

common_strings = {
    "إلغاء": "cancel",
    "رجوع": "back",
    "عودة": "back",
    "حذف": "delete",
    "إغلاق": "close",
    "بحث": "search",
    "مشاركة": "share",
    "تعديل": "edit",
    "الكل": "all",
    "نعم": "yes",
    "لا": "no",
    "حفظ": "save"
}

english_translations = {
    "cancel": "Cancel",
    "back": "Back",
    "delete": "Delete",
    "close": "Close",
    "search": "Search",
    "share": "Share",
    "edit": "Edit",
    "all": "All",
    "yes": "Yes",
    "no": "No",
    "save": "Save"
}

def update_strings_xml(strings_file, is_english=False):
    with open(strings_file, 'r', encoding='utf-8') as f:
        content = f.read()
    
    insert_idx = content.find('</resources>')
    new_entries = []
    
    for ar, key in common_strings.items():
        if f'name="{key}"' not in content:
            val = english_translations[key] if is_english else ar
            new_entries.append(f'    <string name="{key}">{val}</string>\n')
            
    if new_entries:
        content = content[:insert_idx] + "".join(new_entries) + content[insert_idx:]
        with open(strings_file, 'w', encoding='utf-8') as f:
            f.write(content)

update_strings_xml('app/src/main/res/values/strings.xml', False)
update_strings_xml('app/src/main/res/values-en/strings.xml', True)

def refactor_kotlin_files():
    for root, dirs, files in os.walk('app/src/main/java/com/siraj/app/features'):
        for file in files:
            if file.endswith('.kt') and not file.endswith('ViewModel.kt'):
                path = os.path.join(root, file)
                with open(path, 'r', encoding='utf-8') as f:
                    content = f.read()
                
                modified = False
                for ar, key in common_strings.items():
                    # Replace Text("ar")
                    pattern1 = f'Text\\("{ar}"\\)'
                    repl1 = f'Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.{key}))'
                    if re.search(pattern1, content):
                        content = re.sub(pattern1, repl1, content)
                        modified = True
                        
                    # Replace Text("ar", 
                    pattern2 = f'Text\\("{ar}",'
                    repl2 = f'Text(androidx.compose.ui.res.stringResource(com.siraj.app.R.string.{key}),'
                    if re.search(pattern2, content):
                        content = re.sub(pattern2, repl2, content)
                        modified = True
                        
                    # Replace contentDescription = "ar"
                    pattern3 = f'contentDescription = "{ar}"'
                    repl3 = f'contentDescription = androidx.compose.ui.res.stringResource(com.siraj.app.R.string.{key})'
                    if re.search(pattern3, content):
                        content = re.sub(pattern3, repl3, content)
                        modified = True
                
                if modified:
                    with open(path, 'w', encoding='utf-8') as f:
                        f.write(content)

refactor_kotlin_files()
