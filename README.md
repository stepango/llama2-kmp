# llama2-kmp

![llama2-kmp](docs/llama2-kmp.png)

This is the Kotlin Multiplatform implementation of [Andrej Karpathy](https://karpathy.ai/)'s [llama2.c](https://github.com/karpathy/llama2.c) project.

## How to Run

![15M](docs/model_15M.png)

### Run with gradle

JVM:
```shell
./gradlew :jvmApp:run --args='stories15M.bin 0.9 512 "The little girl named Oxana"'
```

Windows:
```powershell
.\gradlew.bat llama2:runDebugExecutableMingwX64 -PrunArgs="stories15M.bin 0.9 512 'The little girl named Oxana'"
```

NodeJs:
```shell
llama2:jsNodeRun -PrunArgs='stories15M.bin 0.9 512 "The little girl named Oxana"'
```

In addition to `checkpoint`, other parameters are also supported:

```shell
./gradlew :jvmApp:run --args='/path/to/model.bin 0.9 256 "One day, Lily met a Shoggoth"'
```

Parameter description:

- `/path/to/model.bin`: Mandatory model file path.
- `0.9`: Optional parameter, sets the threshold, default is 1.0.
- `256`: Optional parameter, sets the cache size, default is 512.
- `One day, Lily met a Shoggoth`: Optional parameter, sets the prompt for generating the story.

Example output:

>Once upon a time, there was a little girl named Lily. She loved to play outside in the sunshine. One day, she saw a big, red ball in the sky. It was the sun! She thought it was so pretty.
Lily wanted to play with the ball, but it was too high up in the sky. She tried to jump and reach it, but she couldn't. Then, she had an idea. She would use a stick to knock the ball down.
Lily found a stick and tried to hit the ball. But the stick was too short. She tried again and again, but she couldn't reach it. She felt sad.
Suddenly, a kind man came by and saw Lily. He asked her what was wrong. Lily told him about the ball. The man smiled and said, "I have a useful idea!" He took out a long stick and used it to knock the ball down. Lily was so happy! She thanked the man and they played together in the sunshine.
>
>Once upon a time, there was a little girl named Lily. She loved to play outside in the sunshine. One day, she saw a big, red
>
> achieved tok/s: 68.054444
