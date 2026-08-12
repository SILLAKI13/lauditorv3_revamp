with open('app/src/main/java/com/digicoffer/lauditor/Meetings/ViewModels/CreateEvent.kt', 'r', encoding='utf-8') as f:
    content = f.read()

# Revert .NAME = to .name =
content = content.replace('.NAME =', '.name =')
# Fix specifically timeZonesDO
content = content.replace('timeZonesDO.name', 'timeZonesDO.NAME')

# Fix Context type mismatch
content = content.replace('WebServiceHelper.callHttpWebService(this, context,', 'WebServiceHelper.callHttpWebService(this, requireContext(),')

# Fix JSONArray? errors
content = content.replace('jsonArray.length()', 'jsonArray!!.length()')
content = content.replace('jsonArray.getJSONObject(', 'jsonArray!!.getJSONObject(')
content = content.replace('jsonArray.getJSONArray(', 'jsonArray!!.getJSONArray(')

# Fix Unresolved reference 'isIs_linked_with_timesheet'
content = content.replace('.isIs_linked_with_timesheet()', '.is_linked_with_timesheet')
content = content.replace('.isIs_linked_with_timesheet', '.is_linked_with_timesheet')

content = content.replace('.isTimesheet_added()', '.timesheet_added')
content = content.replace('.isTimesheet_added', '.timesheet_added')

content = content.replace('.isAll_day()', '.all_day')
content = content.replace('.isAll_day', '.all_day')

# Fix Context
content = content.replace('context,', 'requireContext(),')
content = content.replace('context)', 'requireContext())')
content = content.replace('context!!', 'requireContext()')

with open('app/src/main/java/com/digicoffer/lauditor/Meetings/ViewModels/CreateEvent.kt', 'w', encoding='utf-8') as f:
    f.write(content)
