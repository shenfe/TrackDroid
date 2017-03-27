// ==UserScript==
// @name         AutoDownloader
// @version      0.1
// @description  try to take over the world!
// @author       Hengwu
// @include      https://apkpure.com/*
// ==/UserScript==

(function() {
    'use strict';

    var MainFunc = function () {
        var appset = JSON.parse(localStorage.getItem('categories_free_mass'));
        var appsetsize = appset.length;
        var appindex = parseInt(localStorage.getItem('this_app_index'));

        var GetAppName = function () {
            var url = location.href;
            var pattern = 'id=';
            var pos = url.indexOf(pattern);
            if(pos < 0) return null;
            return url.substr(pos + pattern.length);
        };
        var NextApp = function () {
            appindex++;
            if(appindex >= appsetsize || appindex < 0) return null;
            while(appset[appindex] == '') {
                appindex++;
                if(appindex >= appsetsize || appindex < 0) return null;
            }
            localStorage.setItem('this_app_index', appindex);
            location.href = 'https://apkpure.com/store/apps/details?id=' + appset[appindex];
        };
        var DownloadCurApp = function () {
            var selector = 'div.main>div.left>div.box>div.ny-down>a.ga.da';
            var downloadBtn = document.querySelector(selector);
            if(downloadBtn == null) {
            	console.log('Cannot find the download-button!');
            	return false;
            }
            console.log('Find the download-button!');

            var fileSize = downloadBtn.getElementsByTagName('span');
            if(fileSize.length == 0) {
                console.log('Cannot find the file-size!');
            	return false;
            }
            fileSize = fileSize[0];
            fileSize = fileSize.innerText;
            console.log('FileSize: ' + fileSize);
            if(fileSize.length < 5) return false;
            if(fileSize.indexOf('GB') >= 0) return false;
            if(fileSize.indexOf('MB') >= 0) {
                var pos0 = 1, pos1 = fileSize.indexOf(' ');
                fileSize = parseFloat(fileSize.substring(pos0, pos1));
                if(isNaN(fileSize)) return false;
                if(fileSize >= 10) return false;
            }

            appset[appindex] = '';
            localStorage.setItem('categories_free_mass', JSON.stringify(appset));
            downloadBtn.click();
            console.log('Click the download-button!');
            return true;
        };

        setTimeout(function () {
            var ifDownload = DownloadCurApp();
            setTimeout(function () {
            	console.log('Go to the next app!');
                NextApp();
            }, ifDownload ? 10000 : 1000);
        }, 1000);
    };
    
    var oldonload = window.onload;
    if(typeof window.onload != 'function') {
        window.onload = MainFunc;
    } else {
        window.onload = function() {
            oldonload();
            MainFunc();
        };
    }
    
    //MainFunc();
})();