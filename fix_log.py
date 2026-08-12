with open('app/src/main/java/com/digicoffer/lauditor/Meetings/ViewModels/CreateEvent.kt', 'r', encoding='utf-8') as f:
    content = f.read()

content = content.replace('Log.d("List_Item", selected_tm_list[i].name)', 'Log.d("List_Item", selected_tm_list[i].name ?: "")')
content = content.replace('AndroidUtils.showToast(e.message, context)', 'AndroidUtils.showToast(e.message ?: "", context)')
content = content.replace('AndroidUtils.showAlert(e.message, requireActivity())', 'AndroidUtils.showAlert(e.message ?: "", requireActivity())')
content = content.replace('AndroidUtils.showAlert(e.message, activity)', 'AndroidUtils.showAlert(e.message ?: "", requireActivity())')
content = content.replace('AndroidUtils.showToast(e.message, requireContext())', 'AndroidUtils.showToast(e.message ?: "", requireContext())')

with open('app/src/main/java/com/digicoffer/lauditor/Meetings/ViewModels/CreateEvent.kt', 'w', encoding='utf-8') as f:
    f.write(content)
