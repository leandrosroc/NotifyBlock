# NotifyBlock

NotifyBlock é um aplicativo Android que permite monitorar notificações para bloquear e armazenar histórico de notificações com base em uma palavra-chave específica. Ele fornece funcionalidades para criar notificações de teste e limpar o histórico de notificações.

## Funcionalidades

- **Salvar Palavra-chave:** Permite ao usuário definir uma palavra-chave que será usada para filtrar notificações.
- **Criar Notificação de Teste:** Permite criar uma notificação de teste com a palavra-chave definida.
- **Visualizar Histórico de Notificações:** Mostra o histórico de notificações que contêm a palavra-chave.
- **Limpar Histórico:** Permite ao usuário limpar todo o histórico de notificações.
- **Verificar Permissões:** Verifica se o serviço de escuta de notificações está ativado e solicita permissões se necessário.
- **Alternar Serviço de Notificações:** Permite ativar ou desativar o serviço que escuta notificações.

## Requisitos

- Android 7.0 (Nougat) ou superior.
- Permissões de acesso a notificações.

## Instalação

1. Clone o repositório:
    ```bash
    git clone https://github.com/leandrosroc/NotifyBlock.git
    ```

2. Abra o projeto no Android Studio.

3. Compile e execute o aplicativo no seu dispositivo ou emulador.

## Configuração

### Configuração de Permissões

Certifique-se de que o serviço de escuta de notificações está ativado:
1. Abra o aplicativo e clique no botão "Abrir Configurações".
2. Ative o serviço de escuta de notificações para o NotifyBlock.

### Salvando Palavra-chave

1. No aplicativo, insira a palavra-chave desejada no campo "Palavra-chave".
2. Clique no botão "Salvar Palavra-chave" para armazenar a palavra-chave.

### Criando Notificação de Teste

1. Insira a palavra-chave desejada no campo "Palavra-chave".
2. Clique no botão "Criar Notificação" para gerar uma notificação de teste.

### Visualizando e Limpando o Histórico

1. O histórico de notificações será exibido automaticamente na tela principal.
2. Clique no botão "Limpar Histórico" para remover todas as entradas do histórico.

## Estrutura do Projeto

- `MainActivity.java`: Atividade principal que gerencia a interface do usuário e interage com o serviço de notificações.
- `MyNotificationListenerService.java`: Serviço que escuta notificações e filtra com base na palavra-chave.
- `NotificationHistoryDatabaseHelper.java`: Classe auxiliar para manipular o banco de dados do histórico de notificações.

## Contribuições

Sinta-se à vontade para contribuir para este projeto. Se você encontrar bugs ou tiver sugestões de melhorias, por favor, abra uma issue ou envie um pull request.

## Licença

Este projeto está licenciado sob a [MIT License](LICENSE).

## Contato

Se você tiver alguma dúvida, entre em contato com [leormt4@exemplo.com](mailto:leormt4@exemplo.com).
