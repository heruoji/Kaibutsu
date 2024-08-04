package org.example.kaibutsu.core.downloader;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitUntilState;
import reactor.core.publisher.Mono;

public class DynamicDownloader implements Downloader {

    private Playwright playwright;
    private Browser browser;
    private Page page;

    public DynamicDownloader() {
        initializePlaywright();
    }

    @Override
    public Mono<Response> download(Request request) {
        return Mono.create(sink -> {
            try {
                sink.success(navigateAndExtract(request));
            } catch (Exception e) {
                sink.error(new DownloaderException("Failed to download. Request: " + request, e));
            }
        });
    }

    private Response navigateAndExtract(Request request) {
        ensurePage();
        com.microsoft.playwright.Response pwResponse = page.navigate(request.getUrl(), new Page.NavigateOptions().setWaitUntil(WaitUntilState.NETWORKIDLE));
        if (!pwResponse.ok()) {
            throw new DownloaderException("Failed to navigate to URL: " + request.getUrl() + ". HTTP status: " + pwResponse.status());
        }
        page.waitForLoadState(LoadState.LOAD);
        String content = page.content();
        return new Response(request.getUrl(), content.getBytes(), request);
    }

    private void ensurePage() {
        if (page == null || page.isClosed()) {
            page = browser.newPage();
        }
    }

    @Override
    public void close() {
        finalizePlaywright();
    }


    private void initializePlaywright() {
        this.playwright = Playwright.create();
        this.browser = playwright
                .chromium()
                .launch(new BrowserType.LaunchOptions().setHeadless(false).setSlowMo(50));
    }

    private void finalizePlaywright() {
        if (page != null && !page.isClosed()) {
            page.close();
        }
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
    }
}
