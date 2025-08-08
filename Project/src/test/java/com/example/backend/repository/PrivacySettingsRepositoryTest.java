package com.example.backend.repository;

import com.example.backend.entity.PrivacySettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@SpringJUnitConfig
class PrivacySettingsRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private PrivacySettingsRepository privacySettingsRepository;

    private UUID userId1;
    private UUID userId2;
    private PrivacySettings settings1;
    private PrivacySettings settings2;

    @BeforeEach
    void setUp() {
        userId1 = UUID.randomUUID();
        userId2 = UUID.randomUUID();

        settings1 = new PrivacySettings(userId1);
        settings1.setShareAnalytics(true);
        settings1.setVisibilityLevel(PrivacySettings.VisibilityLevel.PUBLIC);
        settings1.setEnableSocialFeatures(true);

        settings2 = new PrivacySettings(userId2);
        settings2.setShareAnalytics(false);
        settings2.setVisibilityLevel(PrivacySettings.VisibilityLevel.PRIVATE);
        settings2.setEnableSocialFeatures(false);
    }

    @Test
    void testSaveAndFindByUserId() {
        // When
        privacySettingsRepository.save(settings1);
        entityManager.flush();

        // Then
        Optional<PrivacySettings> found = privacySettingsRepository.findByUserId(userId1);
        assertThat(found).isPresent();
        assertThat(found.get().getUserId()).isEqualTo(userId1);
        assertThat(found.get().getShareAnalytics()).isTrue();
        assertThat(found.get().getVisibilityLevel()).isEqualTo(PrivacySettings.VisibilityLevel.PUBLIC);
    }

    @Test
    void testFindUsersWhoShareAnalytics() {
        // Given
        privacySettingsRepository.save(settings1); // shares analytics
        privacySettingsRepository.save(settings2); // doesn't share analytics
        entityManager.flush();

        // When
        List<PrivacySettings> usersWhoShare = privacySettingsRepository.findUsersWhoShareAnalytics();

        // Then
        assertThat(usersWhoShare).hasSize(1);
        assertThat(usersWhoShare.get(0).getUserId()).isEqualTo(userId1);
        assertThat(usersWhoShare.get(0).getShareAnalytics()).isTrue();
    }

    @Test
    void testFindByVisibilityLevel() {
        // Given
        privacySettingsRepository.save(settings1); // PUBLIC
        privacySettingsRepository.save(settings2); // PRIVATE
        
        PrivacySettings settings3 = new PrivacySettings(UUID.randomUUID());
        settings3.setVisibilityLevel(PrivacySettings.VisibilityLevel.PUBLIC);
        privacySettingsRepository.save(settings3);
        entityManager.flush();

        // When
        List<PrivacySettings> publicUsers = privacySettingsRepository.findByVisibilityLevel(PrivacySettings.VisibilityLevel.PUBLIC);

        // Then
        assertThat(publicUsers).hasSize(2);
        assertThat(publicUsers).extracting(PrivacySettings::getVisibilityLevel)
                .containsOnly(PrivacySettings.VisibilityLevel.PUBLIC);
    }

    @Test
    void testExistsByUserId() {
        // Given
        privacySettingsRepository.save(settings1);
        entityManager.flush();

        // When & Then
        assertThat(privacySettingsRepository.existsByUserId(userId1)).isTrue();
        assertThat(privacySettingsRepository.existsByUserId(userId2)).isFalse();
    }

    @Test
    void testGetAnalyticsSharingPreference() {
        // Given
        privacySettingsRepository.save(settings1);
        entityManager.flush();

        // When
        Optional<Boolean> preference = privacySettingsRepository.getAnalyticsSharingPreference(userId1);

        // Then
        assertThat(preference).isPresent();
        assertThat(preference.get()).isTrue();
    }

    @Test
    void testCountUsersWhoShareAnalytics() {
        // Given
        privacySettingsRepository.save(settings1); // shares analytics
        privacySettingsRepository.save(settings2); // doesn't share analytics
        
        PrivacySettings settings3 = new PrivacySettings(UUID.randomUUID());
        settings3.setShareAnalytics(true);
        privacySettingsRepository.save(settings3);
        entityManager.flush();

        // When
        long count = privacySettingsRepository.countUsersWhoShareAnalytics();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testDeleteByUserId() {
        // Given
        privacySettingsRepository.save(settings1);
        privacySettingsRepository.save(settings2);
        entityManager.flush();

        // When
        privacySettingsRepository.deleteByUserId(userId1);
        entityManager.flush();

        // Then
        assertThat(privacySettingsRepository.existsByUserId(userId1)).isFalse();
        assertThat(privacySettingsRepository.existsByUserId(userId2)).isTrue();
    }
}