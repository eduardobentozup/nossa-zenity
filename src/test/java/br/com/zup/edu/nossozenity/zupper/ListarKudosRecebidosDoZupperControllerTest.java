package br.com.zup.edu.nossozenity.zupper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(printOnlyOnFailure = false)
class ListarKudosRecebidosDoZupperControllerTest {

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@Autowired
	private KudoRepository kudoRepository;

	@Autowired
	private ZupperRepository zupperRepository;

	private Zupper sender;

	private Zupper receiver;

	private final String URL = "/zupper/{id}/kudos/recebidos";

	@BeforeEach
	void setup() {

		kudoRepository.deleteAll();
		zupperRepository.deleteAll();

		sender = new Zupper("Zupper Sender",
			"backend developer",
			LocalDate.of(2020, 01, 01),
			"zupper.sender@zup.com.br");

		receiver = new Zupper("Zupper Receiver",
			"backend developer",
			LocalDate.of(2020, 01, 01),
			"zupper.receiver@zup.com.br");

		zupperRepository.saveAll(Arrays.asList(sender, receiver));

		kudoRepository.saveAll(Arrays.asList(
			new Kudo(TipoKudo.AGRADECIMENTO, sender, receiver),
			new Kudo(TipoKudo.IDEIAS_ORIGINAIS, sender, receiver))
		);
	}

	@Test
	@DisplayName("Should return a list of kudos")
	void shouldReturtKudoList() throws Exception {

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.get(URL, receiver.getId())
			.header("Accept-Language", "pt-br");

		String responseAsString = mvc.perform(request)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString(StandardCharsets.UTF_8);

		TypeFactory typeFactory = mapper.getTypeFactory();

		List<KudoResponse> kudos = mapper.readValue(
			responseAsString,
			typeFactory.constructCollectionType(List.class, KudoResponse.class)
		);

		assertThat(kudos)
			.hasSize(2)
			.extracting("nome", "enviadoPor")
			.contains(
				new Tuple("agradecimento", "Zupper Sender"),
				new Tuple("ideias_originais", "Zupper Sender")
			);
	}

	@Test
	@DisplayName("Should return empty list when zupper has not received kudos")
	void shouldReturnEmptyListWhenZupperHasNotReceivedKudos() throws Exception {

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.get(URL, sender.getId())
			.header("Accept-Language", "pt-br");

		String responseAsString = mvc.perform(request)
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString(StandardCharsets.UTF_8);

		TypeFactory typeFactory = mapper.getTypeFactory();

		List<KudoResponse> kudos = mapper.readValue(
			responseAsString,
			typeFactory.constructCollectionType(List.class, KudoResponse.class)
		);

		assertTrue(kudos.isEmpty());
	}

	@Test
	@DisplayName("Should return not found when zupper does not exist")
	void shouldReturnNotFoundWhenZupperDoesNotExist() throws Exception {

		MockHttpServletRequestBuilder request = MockMvcRequestBuilders
			.get(URL, Integer.MAX_VALUE)
			.header("Accept-Language", "pt-br");

		Exception exception = mvc.perform(request)
			.andExpect(status().isNotFound())
			.andReturn()
			.getResolvedException();

		assertTrue(exception instanceof ResponseStatusException);
		assertEquals("Zupper nao cadastrado",((ResponseStatusException) exception).getReason());
	}


}