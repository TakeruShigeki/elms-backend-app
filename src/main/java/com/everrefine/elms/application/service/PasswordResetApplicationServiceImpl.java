package com.everrefine.elms.application.service;

import com.everrefine.elms.application.command.PasswordResetConfirmCommand;
import com.everrefine.elms.application.command.PasswordResetRequestCommand;
import com.everrefine.elms.domain.exception.InvalidValueException;
import com.everrefine.elms.domain.model.passwordreset.PasswordResetToken;
import com.everrefine.elms.domain.model.user.EmailAddress;
import com.everrefine.elms.domain.model.user.User;
import com.everrefine.elms.domain.repository.PasswordResetTokenRepository;
import com.everrefine.elms.domain.repository.UserRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** パスワードリセットアプリケーションサービスの実装。 */
@Service
@RequiredArgsConstructor
public class PasswordResetApplicationServiceImpl implements PasswordResetApplicationService {

  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final JavaMailSender mailSender;

  @Value("${mail.from}")
  private String fromAddress;

  @Value("${password-reset.base-url}")
  private String baseUrl;

  @Override
  @Transactional
  public void requestPasswordReset(PasswordResetRequestCommand command) {
    Optional<User> userOpt;
    try {
      userOpt = userRepository.findUserByEmailAddress(new EmailAddress(command.emailAddress()));
    } catch (InvalidValueException e) {
      // アカウントの存在を秘匿するため、メールアドレスの形式が不正でもエラーにはしない。
      // EmailAddress が投げるのは InvalidValueException であり IllegalArgumentException ではないため、
      // 捕捉する型を誤ると 500 になってしまう。
      return;
    }

    if (userOpt.isEmpty()) {
      return;
    }

    User user = userOpt.get();
    PasswordResetToken resetToken = PasswordResetToken.create(user.id());
    passwordResetTokenRepository.save(resetToken);
    sendPasswordResetEmail(command.emailAddress(), resetToken.token());
  }

  @Override
  @Transactional
  public String confirmPasswordReset(PasswordResetConfirmCommand command) {
    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findByToken(command.token())
            .orElseThrow(() -> new IllegalArgumentException("無効なトークンです"));

    if (resetToken.isExpired()) {
      throw new IllegalArgumentException("トークンの有効期限が切れています");
    }

    if (resetToken.isUsed()) {
      throw new IllegalArgumentException("このトークンはすでに使用されています");
    }

    User user =
        userRepository
            .findUserById(resetToken.userId())
            .orElseThrow(() -> new IllegalArgumentException("ユーザーが見つかりません"));

    String emailAddress = user.emailAddress().value();
    userRepository.updateUser(user.update(null, command.newPassword(), null, null, null, null));
    passwordResetTokenRepository.save(resetToken.markAsUsed());
    sendPasswordResetCompleteEmail(emailAddress);

    return emailAddress;
  }

  /**
   * パスワードリセット完了メールを送信する。
   *
   * @param to 送信先メールアドレス
   */
  private void sendPasswordResetCompleteEmail(String to) {
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(to);
    message.setSubject("【Javaエンジニア養成講座】パスワード再設定が完了しました");
    message.setText(
        """
        Javaエンジニア養成講座をご利用いただきありがとうございます。

        以下のアカウントのパスワード再設定が完了しました。

        メールアドレス：%s

        ※ ご自身で操作していない場合は、お問い合わせください。

        ──────────────────────────────
        Javaエンジニア養成講座
        """
            .formatted(to));
    mailSender.send(message);
  }

  /**
   * パスワードリセットメールを送信する。
   *
   * @param to 送信先メールアドレス
   * @param token パスワードリセットトークン
   */
  private void sendPasswordResetEmail(String to, String token) {
    String resetLink = baseUrl + "/reset-password?token=" + token;
    SimpleMailMessage message = new SimpleMailMessage();
    message.setFrom(fromAddress);
    message.setTo(to);
    message.setSubject("【Javaエンジニア養成講座】パスワード再設定のご案内");
    message.setText(
        """
        Javaエンジニア養成講座をご利用いただきありがとうございます。

        パスワード再設定のリクエストを受け付けました。
        以下のリンクをクリックして、新しいパスワードを設定してください。

        %s

        ※ このリンクは発行から30分間有効です。
        ※ ご自身でリクエストしていない場合は、このメールを無視してください。

        ──────────────────────────────
        Javaエンジニア養成講座
        """
            .formatted(resetLink));
    mailSender.send(message);
  }
}
