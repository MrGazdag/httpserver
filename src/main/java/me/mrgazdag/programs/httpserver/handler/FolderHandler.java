package me.mrgazdag.programs.httpserver.handler;

import me.mrgazdag.programs.httpserver.BadRequestException;
import me.mrgazdag.programs.httpserver.HTTPVersion;
import me.mrgazdag.programs.httpserver.request.HTTPRequest;
import me.mrgazdag.programs.httpserver.resource.FileResource;
import me.mrgazdag.programs.httpserver.resource.HTTPResource;
import me.mrgazdag.programs.httpserver.response.HTTPResponse;
import me.mrgazdag.programs.httpserver.response.HTTPStatusCode;

import java.io.File;
import java.util.function.Function;
import java.util.function.Predicate;

public class FolderHandler implements HTTPHandler, Predicate<HTTPRequest> {
    private final String targetPath;
    private final File folder;
    private final Function<File, HTTPResource> resourceFunction;
    public FolderHandler(String targetPath, File folder) {
        this.targetPath = targetPath;
        this.folder = folder;
        this.resourceFunction = FileResource::create;
    }

    public FolderHandler(String targetPath, File folder, Function<File, HTTPResource> resourceFunction) {
        this.targetPath = targetPath;
        this.folder = folder;
        this.resourceFunction = resourceFunction;
    }

    @Override
    public HTTPResponse handle(HTTPRequest request) throws BadRequestException {
        File f = folder;
        String res = request.getRequestedResource().substring(targetPath.length());
        for (String s : res.split("[/\\\\]")) {
            if (s.equalsIgnoreCase("..") && f == folder) throw new BadRequestException("Backtracing is not allowed");
            f = new File(f, s);
            if (!f.exists()) return null;
        }
        HTTPResource result = resourceFunction.apply(f);
        if (result == null) return null;
        return new HTTPResponse(HTTPStatusCode.HTTP_200_OK, HTTPVersion.VERSION_1_1).resource(result);
    }

    @Override
    public boolean test(HTTPRequest request) {
        return request.getRequestedResource().startsWith(targetPath);
    }
}
