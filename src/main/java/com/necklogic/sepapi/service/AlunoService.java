package com.necklogic.sepapi.service;

import com.necklogic.sepapi.dto.AlunoRequestDTO;
import com.necklogic.sepapi.dto.AlunoResponseDTO;
import com.necklogic.sepapi.model.Aluno;
import com.necklogic.sepapi.model.Professor;
import com.necklogic.sepapi.repository.AlunoRepository;
import com.necklogic.sepapi.strategy.CobrancaStrategy;
import com.necklogic.sepapi.strategy.MensalidadeStrategy;
import com.necklogic.sepapi.strategy.PacoteCreditosStrategy;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlunoService {

    private final AlunoRepository alunoRepository;
    private final MensalidadeStrategy mensalidadeStrategy;
    private final PacoteCreditosStrategy pacoteCreditosStrategy;

    public Page<AlunoResponseDTO> listar(UUID professorId, Pageable pageable){


        return alunoRepository.findAllByProfessorId(professorId, pageable)
                .map(this::mapToDTO);
    }

    public AlunoResponseDTO buscarPorId(UUID id, UUID professorId){
        Aluno aluno = alunoRepository.findByIdAndProfessorId(id, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return mapToDTO(aluno);
    }

    public AlunoResponseDTO criar(AlunoRequestDTO dto, Professor professor){
        Aluno aluno = Aluno.builder()
                .nome(dto.nome())
                .ativo(dto.ativo())
                .tipoCobranca(dto.tipoCobranca())
                .professor(professor)
                .build();

        aplicarStrategy(aluno);

        return mapToDTO(alunoRepository.save(aluno));
    }

    public AlunoResponseDTO atualizar(UUID id, AlunoRequestDTO dto, UUID professorId){
        Aluno aluno = alunoRepository.findByIdAndProfessorId(id, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        aluno.setNome(dto.nome());
        aluno.setAtivo(dto.ativo());
        aluno.setTipoCobranca(dto.tipoCobranca());

        aplicarStrategy(aluno);

        return mapToDTO(alunoRepository.save(aluno));
    }

    public void deletar(UUID id, UUID professorId){
        Aluno aluno = alunoRepository.findByIdAndProfessorId(id, professorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        alunoRepository.delete(aluno);
    }

    private void aplicarStrategy(Aluno aluno){
        CobrancaStrategy strategy = switch(aluno.getTipoCobranca()){
            case MENSALIDADE -> mensalidadeStrategy;
            case PACOTE_CREDITOS -> pacoteCreditosStrategy;
        };

        strategy.processar(aluno);
    }

    private AlunoResponseDTO mapToDTO(Aluno aluno){

        return new AlunoResponseDTO(aluno.getId(), aluno.getNome(),
                                    aluno.isAtivo(), aluno.getTipoCobranca());
    }

}
