package nz.ac.wgtn.swen301.a3.server;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestStatsHTML {
    private StatsServlet statsServlet;
    private LogsServlet logsServlet;

    @BeforeEach
    void setupServlet() {
        statsServlet = new StatsServlet();
        logsServlet = new LogsServlet();
    }

    @AfterEach
    void clearServlet() {
        statsServlet = null;
        logsServlet = null;
    }

    @Test
    void formatTest() throws Exception {
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        statsServlet.doGet(req1, res1);
        var doc = Jsoup.parse(res1.getContentAsString());
        doc.select("table");
        Elements header = doc.select("table").get(0).select("tr").select("th");
        assertEquals(1 + LevelEnum.values().length, header.size(), "Column number error");
    }

    @Test
    void dataCorrectnessTest() throws Exception {
        var om = new ObjectMapper();
        var list = IntStream.range(0, 20).mapToObj(i -> {
//            var r = new SecureRandom().nextInt(LevelEnum.values().length);
            return new LogEvent(String.valueOf(i), "formatTest", "formatTest" + i, LevelEnum.INFO, "log" + i);
        }).collect(Collectors.toUnmodifiableList());
        list.forEach(e -> {
            try {
                var req = new MockHttpServletRequest();
                var res = new MockHttpServletResponse();
                req.setContent(om.writeValueAsBytes(e));
                logsServlet.doPost(req, res);
            } catch (Exception ignored) {
            }
        });
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        statsServlet.doGet(req1, res1);
        var doc = Jsoup.parse(res1.getContentAsString());
        doc.select("table");
        var cols = doc.select("table").get(0).select("tr");
        IntStream.range(1, cols.size()).forEach(i -> assertEquals("1", cols.get(i).select("td").get(1 + LevelEnum.INFO.ordinal()).text()));
        assertEquals(1 + list.size(), cols.size(), "row size error");
    }

    @Test
    void contentTypeTest() throws Exception {
        MockHttpServletRequest req1 = new MockHttpServletRequest();
        MockHttpServletResponse res1 = new MockHttpServletResponse();
        statsServlet.doGet(req1, res1);
        assertEquals("text/html", res1.getContentType());
    }
}
