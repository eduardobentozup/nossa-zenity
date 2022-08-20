package br.com.zup.edu.nossozenity.zupper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class RemoverCertificadoControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private CertificadoRepository certificadoRepository;

	@Autowired
	private ZupperRepository zupperRepository;

	@BeforeEach
	void setup(){
		certificadoRepository.deleteAll();
		zupperRepository.deleteAll();
	}

	@Test
	@DisplayName("Should delete a certificate")
	void shouldDeleteCertificate() throws Exception {
		Zupper zupper = new Zupper(
			"Fulano de Souza",
			"Developer II",
			LocalDate.now(),
			"fulano.souza@zup.com.br"
		);
		zupperRepository.save(zupper);

		Certificado certificado = new Certificado(
			"Orange Talents",
			"Zup Innovation",
			"certificado.zup.com.br/funalo.souza",
			zupper,
			TipoCertificado.CURSO
		);
		certificadoRepository.save(certificado);

		MockHttpServletRequestBuilder request = delete("/certificados/{id}", certificado.getId())
			.contentType(MediaType.APPLICATION_JSON);


		mvc.perform(request)
			.andExpect(status().isNoContent());

		assertFalse(certificadoRepository.findById(certificado.getId()).isPresent());
	}

	@Test
	@DisplayName("Should not delete when not exists")
	void shouldNotDeleteWhenNotExists() throws Exception {

		MockHttpServletRequestBuilder request = delete("/certificados/{id}", Integer.MAX_VALUE)
			.contentType(MediaType.APPLICATION_JSON);

		Exception exception = mvc.perform(request)
			.andExpect(status().isNotFound())
			.andReturn()
			.getResolvedException();

		assertNotNull(exception);
		assertEquals(ResponseStatusException.class, exception.getClass());
		assertEquals("Certificado não cadastrado.", ((ResponseStatusException) exception).getReason());
	}
}