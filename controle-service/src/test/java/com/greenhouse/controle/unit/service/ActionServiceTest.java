package com.greenhouse.controle.unit.service;

import com.greenhouse.controle.dto.ActionRequest;
import com.greenhouse.controle.dto.ActionResponse;
import com.greenhouse.controle.exception.ResourceNotFoundException;
import com.greenhouse.controle.model.Action;
import com.greenhouse.controle.model.ActionStatus;
import com.greenhouse.controle.model.ActionType;
import com.greenhouse.controle.model.Equipement;
import com.greenhouse.controle.repository.ActionRepository;
import com.greenhouse.controle.repository.EquipementRepository;
import com.greenhouse.controle.service.ActionService;
import com.greenhouse.controle.testutil.TestDataBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ActionService Unit Tests")
class ActionServiceTest {

    @Mock
    private ActionRepository actionRepository;

    @Mock
    private EquipementRepository equipementRepository;

    @InjectMocks
    private ActionService actionService;

    private Action testAction;
    private Equipement testEquipement;

    @BeforeEach
    void setUp() {
        testEquipement = TestDataBuilder.createVentilateur();
        testAction = TestDataBuilder.createPendingAction(1L);
    }

    @Nested
    @DisplayName("createAction")
    class CreateAction {

        @Test
        @DisplayName("should create action successfully when equipement exists")
        void shouldCreateActionSuccessfully() {
            // Given
            ActionRequest request = TestDataBuilder.anAction().buildRequest();
            when(equipementRepository.findById(1L)).thenReturn(Optional.of(testEquipement));
            when(actionRepository.save(any(Action.class))).thenReturn(testAction);

            // When
            ActionResponse response = actionService.createAction(request);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(ActionStatus.EN_ATTENTE);
            verify(actionRepository).save(any(Action.class));
        }

        @Test
        @DisplayName("should throw exception when equipement not found")
        void shouldThrowExceptionWhenEquipementNotFound() {
            // Given
            ActionRequest request = ActionRequest.builder()
                    .equipementId(999L)
                    .type(ActionType.ALLUMER)
                    .build();
            when(equipementRepository.findById(999L)).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> actionService.createAction(request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(actionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("executeAction")
    class ExecuteAction {

        @Test
        @DisplayName("should execute pending action successfully")
        void shouldExecutePendingActionSuccessfully() {
            // Given
            when(actionRepository.findById(1L)).thenReturn(Optional.of(testAction));
            when(equipementRepository.findById(1L)).thenReturn(Optional.of(testEquipement));

            Action executedAction = TestDataBuilder.anAction().executed().build();
            when(actionRepository.save(any(Action.class))).thenReturn(executedAction);

            // When
            ActionResponse response = actionService.executeAction(1L);

            // Then
            assertThat(response).isNotNull();
            assertThat(response.getStatus()).isEqualTo(ActionStatus.EXECUTEE);
        }

        @Test
        @DisplayName("should throw exception for already executed action")
        void shouldThrowExceptionForAlreadyExecutedAction() {
            // Given
            Action executedAction = TestDataBuilder.anAction().executed().build();
            when(actionRepository.findById(1L)).thenReturn(Optional.of(executedAction));

            // When/Then
            assertThatThrownBy(() -> actionService.executeAction(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already");
        }
    }

    @Nested
    @DisplayName("getAllActions")
    class GetAllActions {

        @Test
        @DisplayName("should return paginated actions")
        void shouldReturnPaginatedActions() {
            // Given
            Action action1 = TestDataBuilder.anAction().withId(1L).build();
            Action action2 = TestDataBuilder.anAction().withId(2L).build();
            Page<Action> page = new PageImpl<>(Arrays.asList(action1, action2));

            when(actionRepository.findAll(any(Pageable.class))).thenReturn(page);
            when(equipementRepository.findById(any())).thenReturn(Optional.of(testEquipement));

            // When
            Page<ActionResponse> responses = actionService.getAllActions(0, 10);

            // Then
            assertThat(responses.getContent()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("getActionsByStatus")
    class GetActionsByStatus {

        @Test
        @DisplayName("should return actions filtered by status")
        void shouldReturnActionsFilteredByStatus() {
            // Given
            Action pendingAction = TestDataBuilder.anAction()
                    .withStatus(ActionStatus.EN_ATTENTE)
                    .build();
            Page<Action> page = new PageImpl<>(Arrays.asList(pendingAction));

            when(actionRepository.findByStatus(eq(ActionStatus.EN_ATTENTE), any(Pageable.class)))
                    .thenReturn(page);
            when(equipementRepository.findById(any())).thenReturn(Optional.of(testEquipement));

            // When
            Page<ActionResponse> responses = actionService.getActionsByStatus(
                    ActionStatus.EN_ATTENTE, 0, 10
            );

            // Then
            assertThat(responses.getContent()).hasSize(1);
            assertThat(responses.getContent().get(0).getStatus())
                    .isEqualTo(ActionStatus.EN_ATTENTE);
        }
    }
}
