package com.jhssong.univcodeserver.presentation.admin;

import com.jhssong.univcodeserver.application.admin.AdminLoginAttemptService;
import com.jhssong.univcodeserver.application.admin.AdminMailService;
import com.jhssong.univcodeserver.application.admin.AdminMemberRow;
import com.jhssong.univcodeserver.application.admin.AdminService;
import com.jhssong.univcodeserver.application.apikey.RateLimitService;
import com.jhssong.univcodeserver.application.resend.ResendMetricsService;
import com.jhssong.univcodeserver.domain.apikey.entity.ApiKeyStatus;
import com.jhssong.univcodeserver.domain.apikey.repository.ApiKeyRepository;
import com.jhssong.univcodeserver.domain.member.repository.MemberRepository;
import com.jhssong.univcodeserver.global.auth.AdminAuthInterceptor;
import com.jhssong.univcodeserver.global.config.AdminProperties;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.global.logging.ClientIpUtils;
import java.time.LocalDate;
import java.util.List;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminWebController {

    private final AdminProperties adminProperties;
    private final AdminService adminService;
    private final AdminMailService adminMailService;
    private final AdminLoginAttemptService adminLoginAttemptService;
    private final PasswordEncoder passwordEncoder;
    private final MemberRepository memberRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final ResendMetricsService resendMetricsService;

    // ── Auth ──────────────────────────────────────────

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String password,
                        HttpServletRequest request, Model model) {
        String ip = ClientIpUtils.clientIp(request);
        if (adminLoginAttemptService.isBlocked(ip)) {
            model.addAttribute("error", "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요.");
            return "admin/login";
        }
        if (adminProperties.getUsername().equals(username)
                && passwordEncoder.matches(password, adminProperties.getPassword())) {
            adminLoginAttemptService.recordSuccess(ip);
            HttpSession session = request.getSession(true);
            session.setAttribute(AdminAuthInterceptor.SESSION_KEY, true);
            return "redirect:/admin/dashboard";
        }
        adminLoginAttemptService.recordFailure(ip, request);
        model.addAttribute("error", "아이디 또는 비밀번호가 올바르지 않습니다.");
        return "admin/login";
    }

    @PostMapping("/logout")
    public String logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) session.invalidate();
        return "redirect:/admin/login";
    }

    // ── Dashboard (stats + members, single page) ──────

    @GetMapping({"", "/dashboard"})
    public String dashboard(Model model) {
        List<AdminMemberRow> members = adminService.findAllMembers();
        model.addAttribute("totalMembers", memberRepository.count());
        model.addAttribute("totalApiKeys", apiKeyRepository.count());
        model.addAttribute("activeApiKeys", apiKeyRepository.countByStatus(ApiKeyStatus.ACTIVE));
        model.addAttribute("todayApiCalls", apiKeyRepository.sumDailyCallCountByDate(LocalDate.now()));
        model.addAttribute("members", members);
        model.addAttribute("pendingRequests", members.stream()
                .filter(AdminMemberRow::hasPendingIssuanceRequest)
                .toList());
        model.addAttribute("dailyLimit", RateLimitService.DAILY_LIMIT);
        model.addAttribute("resendUsage", resendMetricsService.getUsage(30));
        return "admin/dashboard";
    }

    @PostMapping("/members/create")
    public String createMember(@RequestParam String email, @RequestParam String affiliation,
                               @RequestParam String password, RedirectAttributes redirectAttributes) {
        try {
            adminService.createMember(email, affiliation, password);
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getErrorCode().getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/members/{id}/delete")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteMember(id);
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getErrorCode().getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // ── API Keys (member 1:1, on the same page) ───────

    @PostMapping("/members/{id}/api-key/issue")
    public String issueApiKey(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.createApiKey(id, "관리자 수동 발급");
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getErrorCode().getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/api-keys/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.approveApiKey(id);
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getErrorCode().getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/api-keys/{id}/revoke")
    public String revoke(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.revokeApiKey(id);
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getErrorCode().getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/api-keys/{id}/delete")
    public String deleteApiKey(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminService.deleteApiKey(id);
        } catch (CustomException e) {
            redirectAttributes.addFlashAttribute("error", e.getErrorCode().getMessage());
        }
        return "redirect:/admin/dashboard";
    }

    // ── Manual mail ────────────────────────────────────

    @PostMapping("/mail/send")
    public String sendMail(@RequestParam String to, @RequestParam String subject, @RequestParam String body,
                           RedirectAttributes redirectAttributes) {
        adminMailService.sendManual(to, subject, body);
        redirectAttributes.addFlashAttribute("success", "메일 발송을 요청했습니다. 실제 발송 결과는 서버 로그에서 확인하세요.");
        return "redirect:/admin/dashboard";
    }
}
