package com.goit;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.templateresolver.FileTemplateResolver;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet (value = "/time")
public class ThymeleafServlet extends HttpServlet {

    private TemplateEngine engine;
    private static final String TIMEZONE = "timezone";

    private static final String LAST_TIMEZONE = "lastTimezone";

    private static final String ABSOLUTE_PATH = "C:\\Users\\kathe\\IdeaProjects\\Module_9_Dev\\src\\main\\templates\\";

    @Override
    public void init(){
        engine = new TemplateEngine();
        FileTemplateResolver resolver = new FileTemplateResolver();
        resolver.setPrefix(ABSOLUTE_PATH);
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML5");
        resolver.setOrder(engine.getTemplateResolvers().size());
        resolver.setCacheable(false);
        engine.addTemplateResolver(resolver);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("text/html; charset=utf-8");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        ZoneId zoneId;
        if (req.getParameterMap().containsKey(TIMEZONE)) {
            zoneId = getTimezoneParameterFromQuery(req);
            resp.addCookie(new Cookie(LAST_TIMEZONE, zoneId.toString()));
        } else {
            zoneId = getTimezoneParameterFromCookie(req);
        }
        LocalDateTime timeWithZoneId = LocalDateTime.now(zoneId);
        Context simpleContext = new Context(
                req.getLocale(),
                Map.of("queryParams", timeWithZoneId.format(formatter) + " " + zoneId)

        );
        engine.process("time", simpleContext, resp.getWriter());
        resp.getWriter().close();
    }

    private static ZoneId getTimezoneParameterFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (Arrays.stream(cookies)
                .noneMatch(cookie -> cookie.getName().equals(LAST_TIMEZONE))
        ){
            return ZoneId.of("UTC");
        } else {
            String timezone = Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(LAST_TIMEZONE))
                    .map(Cookie::getValue)
                    .collect(Collectors.joining());
            return ZoneId.of(timezone);
        }
    }

    private ZoneId getTimezoneParameterFromQuery(HttpServletRequest req) {
        if (!req.getParameterMap().containsKey(TIMEZONE)) {
            return ZoneId.of("UTC");
        } else {
            String timezone = req.getParameter(TIMEZONE);
            return ZoneId.of(timezone.replace(" ", "+"));
        }
    }
}
