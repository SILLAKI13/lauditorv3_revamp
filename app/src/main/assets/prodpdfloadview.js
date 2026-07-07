import * as pdfjsLib from 'https://cdnjs.cloudflare.com/ajax/libs/pdf.js/2.10.377/pdf.min.js';
        
        var decryptUrl = 'https://api.digicoffer.com/professional/v3/decrypt';
        const docfilepdfUrl = 'https://prod.utils.doc2pdf.digicoffer.com/api/v1/docfile2pdf';
        const doctopdfUrl = 'https://prod.utils.doc2pdf.digicoffer.com/api/v1/doc2pdf'
        var token = '';
        var docid = '';
        var viewdecrydocto = '';
        var docType = '';
        var isdoc = false;
        var fileurl = ''
        var postBolb = ''
        async function fetchPdf(apiUrl) {
            try {
                const formData = new FormData();
                if (apiUrl == docfilepdfUrl && viewdecrydocto == "decrypt"){
                    const fileBlob = new Blob([postBolb], { type: docType });
                    formData.append('file', fileBlob, 'blob');
                }else if (apiUrl == doctopdfUrl && viewdecrydocto == "docto"){
                    formData.append("url", fileurl);
                }else{
                    formData.append("docid", docid);
                }

                const response = await fetch(apiUrl, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    },
                    body: formData
                });

                if (!response.ok) {
// <!--                    document.getElementById('h1').textContent = 'Network response was not ok';-->
                    throw new Error('Network response was not ok');
                }
                 const pdfBlob = await response.blob();
                if (viewdecrydocto == "decrypt" && isdoc){
                        postBolb = pdfBlob
                        isdoc = false
                        fetchPdf(docfilepdfUrl)
                }else{
                    var localDocType = docType;
                    if (getContType(docType)){
                        localDocType = 'application/pdf';
                    }
                    const pdfWithMimeType = new Blob([pdfBlob], { type: localDocType });//'application/pdf'
                    const pdfUrl = URL.createObjectURL(pdfWithMimeType);
                    loadPDF(pdfUrl)
                    document.getElementById('pdfViewer').src = pdfUrl;
                }
            } catch (error) {
// <!--                document.getElementById('h1').textContent = 'Network response was Error';-->
                console.error('There was a problem with the fetch operation:', error);
            }
        }

        function getQueryStringParameter(name) {
            const urlParams = new URLSearchParams(window.location.search);
            return urlParams.get(name);
        }

        document.addEventListener("DOMContentLoaded", function() {
                // Get the full URL of the current page
             const url = window.location.href;

             // Create a URL object
             const urlObj = new URL(url);

             // Get the search parameters
             queryParams = new URLSearchParams(urlObj.search);

             // Loop through the query parameters and log them
             queryParams.forEach((value, key) => {
              console.log(`${key}: ${value}`);
            });

             // Example of accessing a specific parameter
             const dataurl = getQueryStringParameter('dataurl');
             token = queryParams.get('token');
             docid = queryParams.get('docid');
             viewdecrydocto = queryParams.get('viewdecrydocto');
             docType = queryParams.get('docType');
             isdoc = getContType(docType);
             let array2 = url.split('dataurl=');
             fileurl = array2[1];
            if (viewdecrydocto == "decrypt") {
                fetchPdf(decryptUrl);
            }else if (viewdecrydocto == "view" && !isdoc){
                document.getElementById('pdfViewer').src = array2[1];
            }else{
                docPdf(doctopdfUrl);
            }
        });
        
        async function docPdf(apiUrl) {
            try {
                const jsonData = {
                    'url': fileurl
                };

                const response = await fetch(apiUrl, {
                    method: 'POST',
                    headers: {
                        'Authorization': `Bearer ${token}`,
                        'Content-Type': 'application/json'
                    },
                    body: JSON.stringify(jsonData)
                });

                if (!response.ok) {
// <!--                    document.getElementById('h1').textContent = 'Network response was not ok';-->
                    throw new Error('Network response was not ok');
                }
                 const pdfBlob = await response.blob();
                    const pdfWithMimeType = new Blob([pdfBlob], { type: 'application/pdf' });
                    const pdfUrl = URL.createObjectURL(pdfWithMimeType);
                    document.getElementById('pdfViewer').src = pdfUrl;
                
            } catch (error) {
// <!--                document.getElementById('h1').textContent = 'Network response was Error';-->
                console.error('There was a problem with the fetch operation:', error);
            }
        }

        async function loadPDF(url) {
            const loadingTask = pdfjsLib.getDocument(url);
            const pdf = await loadingTask.promise;

             // Fetch the first page
             const page = await pdf.getPage(1);
             const scale = 1.5;
             const viewport = page.getViewport({ scale: scale });

            // Prepare canvas using PDF page dimensions
            const canvas = document.getElementById('pdf-canvas');
            const context = canvas.getContext('2d');
            canvas.height = viewport.height;
            canvas.width = viewport.width;

             // Render PDF page into canvas context
            const renderContext = {
                canvasContext: context,
                viewport: viewport
            };
            await page.render(renderContext).promise;
        }

        function getContType(docType) {
            switch (docType) {
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "application/docx" : //docx extension
                return true
            case "application/msword" :
                return true
            case "application/pdf":
                return false
            case "image/png":
                return false
            case "image/gif" :
                return false
            case "image/jpg":
                return false
            case "image/jpg", "image/jpeg":
                return false
            case "application/vnd.ms-excel":
                return true
            case "application/vnd.ms-powerpoint":
                return true
            case "application/vnd.openxmlformats-officedocument.presentationml.presentation":
                return true
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return true
            case "text/csv":
                return true
            case "application/rtf":
                return true
            case "text/rtf":
                return true
            default:
                return true
        }
        }
