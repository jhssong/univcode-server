package com.jhssong.univcodeserver.presentation.member;

import com.jhssong.univcodeserver.application.apikey.ApiKeyService;
import com.jhssong.univcodeserver.application.apikey.ApiKeyUsageService;
import com.jhssong.univcodeserver.application.member.MemberService;
import com.jhssong.univcodeserver.domain.member.entity.Member;
import com.jhssong.univcodeserver.domain.member.repository.MemberRepository;
import com.jhssong.univcodeserver.global.auth.MemberWebInterceptor;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.presentation.apikey.dto.ApiKeyResponse;
import com.jhssong.univcodeserver.presentation.member.dto.MemberSignupRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class MemberWebController {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;
    private final ApiKeyService apiKeyService;
    private final ApiKeyUsageService apiKeyUsageService;

    @GetMapping("/")
    public String root() {
        return "redirect:/my";
    }

    @GetMapping("/signup")
    public String signupPage() {
        return "member/signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String email, @RequestParam String affiliation,
                         @RequestParam String password, HttpServletRequest request,
                         Model model) {
        Long memberId;
        try {
            memberId = memberService.signup(new MemberSignupRequest(email, affiliation, password));
        } catch (CustomException e) {
            model.addAttribute("error", e.getErrorCode().getMessage());
            return "member/signup";
        }
        request.getSession(true).setAttribute(MemberWebInterceptor.SESSION_KEY, memberId);
        return "redirect:/my";
    }

    @GetMapping("/login")
    public String loginPage() {
        return "member/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email, @RequestParam String password,
                        HttpServletRequest request, Model model) {
        Member member = memberRepository.findByEmail(email).orElse(null);
        if (member == null || !passwordEncoder.matches(password, member.getPassword())) {
            model.addAttribute("error", "이메일 또는 비밀번호가 올바르지 않습니다.");
            return "member/login";
        }
        request.getSession(true).setAttribute(MemberWebInterceptor.SESSION_KEY, member.getId());
        return "redirect:/my";
    }

    @GetMapping("/my")
    public String dashboard(HttpServletRequest request, Model model) {
        Long memberId = (Long) request.getAttribute(MemberWebInterceptor.MEMBER_ID_ATTR);
        Member member = memberRepository.findById(memberId).orElseThrow();
        List<ApiKeyResponse> keys = apiKeyService.findAll(memberId);
        ApiKeyResponse key = keys.isEmpty() ? null : keys.getFirst();
        model.addAttribute("email", member.getEmail());
        model.addAttribute("key", key);
        if (key != null) {
            model.addAttribute("dailyUsage", apiKeyUsageService.dailyUsage(key.id(), 14));
            model.addAttribute("monthlyUsage", apiKeyUsageService.monthlyUsage(key.id(), 6));
            model.addAttribute("logs", apiKeyUsageService.recentLogs(key.id()));
        }
        return "member/dashboard";
    }

    @PostMapping("/my/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/login";
    }

    @PostMapping("/my/api-key/reissue")
    public String reissueApiKey(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Long memberId = (Long) request.getAttribute(MemberWebInterceptor.MEMBER_ID_ATTR);
        try {
            apiKeyService.reissue(memberId);
            redirectAttributes.addFlashAttribute("success", "키를 재발급했습니다.");
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getErrorCode().getMessage());
        }
        return "redirect:/my";
    }

    @PostMapping("/my/api-key/request")
    public String requestIssuance(HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Long memberId = (Long) request.getAttribute(MemberWebInterceptor.MEMBER_ID_ATTR);
        try {
            apiKeyService.requestIssuance(memberId, request);
            redirectAttributes.addFlashAttribute("success", "키 발급을 요청했습니다. 관리자 확인 후 처리됩니다.");
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getErrorCode().getMessage());
        }
        return "redirect:/my";
    }
}
