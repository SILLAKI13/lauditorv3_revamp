# Read file with utf-8-sig to automatically strip BOM if present
with open('temp_prefs.xml', 'r', encoding='utf-8-sig') as f:
    content = f.read()

# Replace false with true for is_active
content = content.replace('"is_active":false', '"is_active":true')
content = content.replace('&quot;is_active&quot;:false', '&quot;is_active&quot;:true')

# Write back as clean utf-8 (no BOM)
with open('temp_prefs.xml', 'w', encoding='utf-8') as f:
    f.write(content)

print("BOM stripped and is_active set to true in temp_prefs.xml")
