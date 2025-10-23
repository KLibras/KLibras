<div align="center">

# KLibras

### Aplicativo Android para Aprendizado de Libras

[![Kotlin](https://img.shields.io/badge/Kotlin-100%25-7F52FF.svg)](https://kotlinlang.org/)
[![Android](https://img.shields.io/badge/Android-API%2024+-3DDC84.svg)](https://developer.android.com/)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-1.0-4285F4.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

**Aprenda Linguagem Brasileira de Sinais com reconhecimento em tempo real e inteligência artificial**

[Características](#características) • [Início Rápido](#início-rápido) • [Arquitetura](#arquitetura) • [Contribuindo](#contribuindo)

</div>

---

## Visão Geral

KLibras é um aplicativo Android nativo desenvolvido em Kotlin que utiliza inteligência artificial e visão computacional para ensinar Linguagem Brasileira de Sinais (Libras). Com recursos de reconhecimento em tempo real usando MediaPipe e TensorFlow Lite, o aplicativo oferece uma experiência interativa e gamificada para o aprendizado de Libras.

### Por que KLibras?

- **Reconhecimento em Tempo Real**: Processa gestos de Libras usando a câmera do dispositivo com feedback instantâneo
- **Interface Moderna**: Desenvolvido com Jetpack Compose para uma experiência fluida
- **Gamificação**: Sistema de pontos, rankings e conquistas para motivar o aprendizado
- **Módulos Estruturados**: Conteúdo organizado em módulos progressivos de aprendizado

---

## Características

### Inteligência Artificial e Visão Computacional
- **Reconhecimento de Gestos em Tempo Real**: Utiliza MediaPipe para detectar 21 pontos das mãos e 33 pontos do corpo
- **Processamento On-Device**: Modelos TensorFlow Lite executados diretamente no smartphone
- **Análise de Vídeo**: Grava e analisa sequências de gestos para validação precisa
- **Feedback Visual**: Overlay em tempo real mostrando os pontos detectados durante a captura

### Sistema de Aprendizado
- **Módulos de Ensino**: Conteúdo organizado em módulos temáticos progressivos
- **Dicionário de Sinais (Dex)**: Biblioteca completa de sinais com vídeos de referência
- **Prática Guiada**: Instruções passo a passo com suporte visual em HTML
- **Validação Automática**: Sistema verifica se o sinal foi executado corretamente

### Gamificação e Progresso
- **Sistema de Pontos**: Ganhe pontos ao completar módulos e aprender novos sinais
- **Ranking Global**: Compare seu progresso com outros usuários
- **Estatísticas Pessoais**: Acompanhe quantos sinais você já domina
- **Conquistas**: Desbloqueie sinais e módulos conforme avança

### Experiência do Usuário
- **Autenticação Segura**: Login com JWT e integração com Google Sign-In
- **Interface Intuitiva**: Design moderno com Material Design 3
- **Navegação Fluida**: Bottom navigation bar para acesso rápido às funcionalidades
- **Modo Escuro**: Suporte a tema escuro para conforto visual
- **Vídeos Integrados**: Player nativo com ExoPlayer para tutoriais

---

## Início Rápido

### Pré-requisitos

- **Android Studio** Hedgehog ou superior
- **JDK 11** ou superior
- **Android SDK 24+** (Android 7.0 Nougat ou superior)
- **Dispositivo físico ou emulador** com câmera frontal

### Instalação

#### 1. Clone o Repositório

```bash
git clone https://github.com/KLibras/KLibras.git
cd KLibras
```

#### 2. Configure o Projeto

Abra o projeto no Android Studio e aguarde a sincronização do Gradle.

#### 3. Configure as Variáveis de Ambiente

Crie um arquivo `local.properties` na raiz do projeto (se não existir):

```properties
sdk.dir=/caminho/para/seu/Android/Sdk
```

#### 4. Configure a API

Edite o arquivo de configuração da API em `app/src/main/java/com/br/klibras/core/utils/RetrofitInstance.kt`:

```kotlin
private const val BASE_URL = "https://sua-api-klibras.com/"
```

#### 5. Configure o Google Sign-In (Opcional)

Para habilitar login com Google, adicione seu `google-services.json`:

1. Acesse o [Firebase Console](https://console.firebase.google.com/)
2. Crie/selecione seu projeto
3. Baixe o arquivo `google-services.json`
4. Coloque em `app/google-services.json`

#### 6. Execute o Aplicativo

```bash
# Via Android Studio: clique em Run (▶) ou Shift+F10

# Via linha de comando:
./gradlew installDebug
```

---

## Estrutura do Projeto

```
KLibras/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/br/klibras/
│   │   │   │   ├── core/              # Componentes core
│   │   │   │   │   ├── service/       # Serviços e API
│   │   │   │   │   ├── ui/            # Temas e componentes UI
│   │   │   │   │   └── utils/         # Utilitários
│   │   │   │   ├── features/          # Funcionalidades
│   │   │   │   │   ├── login/         # Tela de login
│   │   │   │   │   ├── register/      # Registro de usuário
│   │   │   │   │   ├── main/          # Tela principal
│   │   │   │   │   ├── camera/        # Captura e análise
│   │   │   │   │   ├── learn/         # Módulos de aprendizado
│   │   │   │   │   ├── gesture/       # Detalhes do sinal
│   │   │   │   │   ├── dex/           # Dicionário de sinais
│   │   │   │   │   ├── ranking/       # Leaderboard
│   │   │   │   │   └── account/       # Perfil do usuário
│   │   │   │   └── shared/            # Componentes compartilhados
│   │   │   ├── assets/                # Modelos de ML
│   │   │   └── res/                   # Recursos (layouts, strings, etc)
│   │   └── androidTest/               # Testes instrumentados
│   └── build.gradle.kts               # Configuração do módulo
├── build.gradle.kts                   # Configuração do projeto
└── settings.gradle.kts                # Configuração do Gradle
```

---

## Arquitetura

### Stack Tecnológico

| Componente | Tecnologia |
|-----------|-----------|
| **Linguagem** | Kotlin 100% |
| **UI Framework** | Jetpack Compose |
| **Arquitetura** | MVVM (Model-View-ViewModel) |
| **Navegação** | Navigation Compose |
| **Câmera** | CameraX |
| **Rede** | Retrofit, OkHttp |
| **Assíncrono** | Kotlin Coroutines, Flow |
| **Autenticação** | JWT, Google Sign-In, Firebase Auth |
| **Player de Vídeo** | ExoPlayer (Media3) |
| **DI** | Manual (ViewModel Factory) |

### Arquitetura MVVM

```
View (Composable)
    ↕
ViewModel (Estado + Lógica)
    ↕
Repository/Service (Dados)
    ↕
API/Local Storage
```

### Fluxo de Reconhecimento de Gestos

1. **Captura**: Usuário grava vídeo do gesto usando CameraX
2. **Processamento Local**: Frames são analisados com MediaPipe
3. **Extração**: Landmarks das mãos e corpo são extraídos
4. **Envio**: Vídeo enviado para API via Retrofit
5. **Validação**: Servidor processa e retorna resultado
6. **Feedback**: UI atualizada com resultado (acerto/erro)
7. **Progresso**: Pontos e conquistas são atualizados

---

## Configuração de Build

### Variantes de Build

```kotlin
buildTypes {
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(...)
    }
    debug {
        isMinifyEnabled = false
        isShrinkResources = false
    }
}
```

### Requisitos Mínimos

- **minSdk**: 24 (Android 7.0)
- **targetSdk**: 36 (Android 15)
- **compileSdk**: 36
- **Java Version**: 11

### Build do APK

```bash
# Debug APK
./gradlew assembleDebug

# Release APK
./gradlew assembleRelease

# Instalar no dispositivo
./gradlew installDebug
```

### Testes

```bash
# Testes unitários
./gradlew test

# Testes instrumentados
./gradlew connectedAndroidTest

# Todos os testes
./gradlew testDebug connectedAndroidTest
```

---

## Principais Funcionalidades

### Tela de Login
- Autenticação com email e senha
- Integração com Google Sign-In
- Validação de credenciais em tempo real
- Splash screen personalizada

### Tela de Aprendizado
- Lista de módulos disponíveis
- Progresso visual de cada módulo
- Acesso aos sinais de cada módulo
- Vídeos explicativos

### Tela de Captura (Câmera)
- Preview em tempo real da câmera frontal
- Overlay visual dos landmarks detectados
- Gravação de vídeo com countdown
- Upload automático para validação
- Feedback visual de sucesso/erro

### Dex (Dicionário)
- Lista completa de sinais disponíveis
- Busca e filtros
- Vídeos de demonstração
- Descrições detalhadas em HTML

### Ranking
- Leaderboard global de usuários
- Pontuação e quantidade de sinais
- Atualização em tempo real

### Perfil do Usuário
- Estatísticas pessoais
- Edição de nome de usuário
- Alteração de senha
- Logout

---

## Desenvolvimento Local

### Executar em Modo Debug

```bash
# Android Studio: Menu Run > Run 'app'
# Ou use Shift + F10

# Via terminal:
./gradlew installDebug
adb shell am start -n com.br.klibras/.features.login.LoginActivity
```

### Logs e Debug

```bash
# Ver logs do app
adb logcat | grep KLibras

# Ver logs específicos
adb logcat -s LoginViewModel CameraViewModel
```

### Verificar Código

```bash
# Lint
./gradlew lint

# Verificar dependências desatualizadas
./gradlew dependencyUpdates
```

---

## Permissões Necessárias

O aplicativo requer as seguintes permissões:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
```

---

## Contribuindo

Contribuições são bem-vindas! Por favor, siga estes passos:

1. Faça fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/nova-funcionalidade`)
3. Commit suas mudanças (`git commit -m 'Adiciona nova funcionalidade'`)
4. Push para a branch (`git push origin feature/nova-funcionalidade`)
5. Abra um Pull Request

### Diretrizes de Desenvolvimento

- Siga as convenções de código Kotlin
- Use Jetpack Compose para novas UIs
- Escreva testes para novas funcionalidades
- Documente código complexo
- Mantenha a arquitetura MVVM
- Use Coroutines para operações assíncronas

---

## Troubleshooting

### Erro de Build

```bash
# Limpar cache do Gradle
./gradlew clean

# Invalidar caches no Android Studio
File > Invalidate Caches / Restart
```

### Câmera não funciona

- Verifique se as permissões foram concedidas
- Teste em dispositivo físico (emulador pode ter limitações)
- Verifique logs: `adb logcat | grep CameraX`


## Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

## Suporte

- **Issues**: [GitHub Issues](https://github.com/KLibras/KLibras/issues)
- **Discussões**: [GitHub Discussions](https://github.com/KLibras/KLibras/discussions)

---

<div align="center">

**Feito para democratizar o acesso ao aprendizado de Libras**

[Reportar Bug](https://github.com/KLibras/KLibras/issues) • [Solicitar Funcionalidade](https://github.com/KLibras/KLibras/issues)

</div>