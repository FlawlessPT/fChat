# TODO fChat

- Spy de staff do canal local.
- Cache de tags (grupos) e saldos: hoje há uma ida à BD por mensagem.
- Teste de integração do `SqlChatPrefsRepository` contra `flawless_test` (padrão `EconomyRepositoryIT`): só há teste unitário com repositório falso.
- Mais canais (staff, etc.).
- Clicar no nome de um jogador no chat escreve o nome dele na caixa de chat (ClickEvent SUGGEST_COMMAND ou, se preferível, o equivalente que preencha só o nome) no componente do nome em ChatFormat.

- Reload de config exige `/freload plugin` (o `ChatConfig` é partilhado por serviço, listeners e comandos).
