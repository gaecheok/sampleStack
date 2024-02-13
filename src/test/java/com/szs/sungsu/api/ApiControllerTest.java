package com.szs.sungsu.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.szs.sungsu.api.request.LoginRequest;
import com.szs.sungsu.api.request.SignupRequest;
import com.szs.sungsu.service.MemberService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.CompletableFuture.delayedExecutor;
import static java.util.concurrent.CompletableFuture.runAsync;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class ApiControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private MemberService memberService;
    @Autowired private EntityManager em;

    private final ObjectMapper om = new ObjectMapper();

    @Test
    @Transactional
    public void 회원가입_정상케이스() throws Exception {
        removeData();

        String userId = "hong";
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";
        String content = om.writeValueAsString(
                new SignupRequest(userId, password, name, regNo));

        this.mockMvc.perform(post("/szs/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @Transactional
    public void 회원가입_유효성오류케이스() throws Exception {
        removeData();

        String userId = ""; // 아이디가 없는 요청
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";

        String content = om.writeValueAsString(
                new SignupRequest(userId, password, name, regNo));
        this.mockMvc.perform(post("/szs/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    @Transactional
    public void 로그인_정상케이스() throws Exception {
        removeData();

        String userId = "hong";
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";
        memberJoin(userId, password, name, regNo);

        String content = om.writeValueAsString(new LoginRequest("hong", "1234"));
        this.mockMvc.perform(post("/szs/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")));
    }

    @Test
    @Transactional
    public void 로그인_인증정보_유효성오류케이스() throws Exception {
        removeData();

        String userId = "hong";
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";
        memberJoin(userId, password, name, regNo);

        String content = om.writeValueAsString(new LoginRequest("hong", "12111134"));
        this.mockMvc.perform(post("/szs/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is4xxClientError());
    }



    @Test
    @Transactional
    public void 스크랩_정상케이스() throws Exception {
        removeData();

        String userId = "hong";
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";
        memberJoin(userId, password, name, regNo);
        String token = getToken(userId, password);

        this.mockMvc.perform(post("/szs/scrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @Rollback(value = false)
    public void 결정세액조회_정상케이스() throws Exception {
        String userId = "hong";
        String password = "1234";
        String name = "홍길동";
        String regNo = "860824-1655068";
        memberJoin(userId, password, name, regNo);
        String token = getToken(userId, password);

        this.mockMvc.perform(post("/szs/scrap")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", "Bearer "+token)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        runAsync(() -> {
            try {
                MvcResult mvcResult = this.mockMvc.perform(get("/szs/refund")
                                .header("Authorization", "Bearer " + token)
                                .accept(MediaType.APPLICATION_JSON))
                        .andDo(print())
                        .andExpect(status().isOk())
                        .andReturn();

                String contentAsString = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
                assertThat(JsonPath.read(contentAsString, "$.이름").toString().length()).isGreaterThan(0);
                assertThat(JsonPath.read(contentAsString, "$.결정세액").toString().length()).isGreaterThan(0);
                assertThat(JsonPath.read(contentAsString, "$.퇴직연금세액공제").toString().length()).isGreaterThan(0);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, delayedExecutor(20, TimeUnit.SECONDS)).join();
    }

    private String getToken(String userId, String password) throws Exception {
        String content = om.writeValueAsString(new LoginRequest(userId, password));
        MvcResult mvcResult = this.mockMvc.perform(post("/szs/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(content)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("accessToken")))
                .andReturn();
        String token = JsonPath.read(mvcResult.getResponse().getContentAsString(), "$.accessToken");
        assertThat(token).isNotEmpty();
        return token;
    }

    private void memberJoin(String userId, String password, String name, String regNo) {
        memberService.joinMember(userId, password, name, regNo);
    }

    private void removeData() {
        em.createQuery("DELETE FROM Tax m").executeUpdate();
        em.createQuery("DELETE FROM Member m").executeUpdate();
    }
}