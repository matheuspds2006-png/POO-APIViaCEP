package APIViaCEP;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

// Aluno: Matheus Pereira da Silva
// RA: 0025260
// Consulta de endereço via API ViaCEP em classe única

public class ViaCep {

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		System.out.println("===== CONSULTA DE ENDEREÇO (API VIACEP) =====");
		System.out.print("Digite o CEP (apenas números ou com traço): ");
		String cepDigitado = sc.nextLine();

		try {
			consultarCep(cepDigitado);
		} catch (Exception e) {
			System.out.println("\nErro: " + e.getMessage());
		} finally {
			sc.close();
		}
	}

	public static void consultarCep(String cep) throws Exception {
		// Remove pontos, traços ou espaços, mantendo apenas os números
		String cepLimpo = cep.replaceAll("\\D", "");

		if (cepLimpo.length() != 8) {
			throw new IllegalArgumentException("CEP inválido! Digite exatamente 8 números.");
		}

		// Monta a URL de consulta da API ViaCEP
		String url = "https://viacep.com.br/ws/" + cepLimpo + "/json/";

		// Cria o cliente e envia a requisição HTTP GET
		HttpClient client = HttpClient.newHttpClient();
		HttpRequest request = HttpRequest.newBuilder()
				.uri(URI.create(url))
				.GET()
				.build();

		HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

		if (response.statusCode() != 200) {
			throw new RuntimeException("Erro ao consultar a API. Código HTTP: " + response.statusCode());
		}

		String json = response.body();

		if (json.contains("\"erro\"")) {
			throw new RuntimeException("CEP não encontrado na base de dados do ViaCEP.");
		}

		String complemento = extrairCampo(json, "complemento");

		System.out.println("\n===== ENDEREÇO ENCONTRADO =====");
		System.out.println("CEP: " + extrairCampo(json, "cep"));
		System.out.println("Logradouro: " + extrairCampo(json, "logradouro"));
		System.out.println("Complemento: " + (complemento.isEmpty() ? "N/A" : complemento));
		System.out.println("Bairro: " + extrairCampo(json, "bairro"));
		System.out.println("Cidade: " + extrairCampo(json, "localidade"));
		System.out.println("Estado (UF): " + extrairCampo(json, "uf"));
	}

	// Método auxiliar para extrair o valor de cada chave do JSON
	private static String extrairCampo(String json, String campo) {
		String chave = "\"" + campo + "\":";
		int inicio = json.indexOf(chave);

		if (inicio == -1) {
			return "";
		}

		inicio = json.indexOf("\"", inicio + chave.length()) + 1;
		int fim = json.indexOf("\"", inicio);

		return json.substring(inicio, fim);
	}
}