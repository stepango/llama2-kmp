# llama2-kmp

![llama2-kmp](docs/llama2-kmp.png)

This is the Kotlin Multiplatform implementation of [Andrej Karpathy](https://karpathy.ai/)'s [llama2.c](https://github.com/karpathy/llama2.c) project.

## How to Run

![15M](docs/model_15M.png)

### Through the Command Line

1. Compile `Llama2.kt` into a jar package;

```shell
kotlinc src/main/kotlin/Llama2.kt -include-runtime -d Llama2.jar
```

1. Execute the generated jar file using the `java -jar` command, passing in the required `checkpoint` path;

```shell
java -jar Llama2.jar /path/to/model.bin
```

In addition to `checkpoint`, other parameters are also supported, as follows:

```shell
java -jar llama2.jar /path/to/model.bin 0.9 256 "One day, Lily met a Shoggoth"
```

Parameter description:

- `/path/to/model.bin`: Mandatory model file path.
- `0.9`: Optional parameter, sets the threshold, default is 1.0.
- `256`: Optional parameter, sets the cache size, default is 512.
- `One day, Lily met a Shoggoth`: Optional parameter, sets the prompt for generating the story.

Output content is as follows:

>Once upon a time, there was a little girl named Lily. She loved to play outside in the sunshine. One day, she saw a big, red ball in the sky. It was the sun! She thought it was so pretty.
Lily wanted to play with the ball, but it was too high up in the sky. She tried to jump and reach it, but she couldn't. Then, she had an idea. She would use a stick to knock the ball down.
Lily found a stick and tried to hit the ball. But the stick was too short. She tried again and again, but she couldn't reach it. She felt sad.
Suddenly, a kind man came by and saw Lily. He asked her what was wrong. Lily told him about the ball. The man smiled and said, "I have a useful idea!" He took out a long stick and used it to knock the ball down. Lily was so happy! She thanked the man and they played together in the sunshine.
>
>Once upon a time, there was a little girl named Lily. She loved to play outside in the sunshine. One day, she saw a big, red
>
> achieved tok/s: 68.054444

### Through Gradle

Alternatively, you can generate and execute the jar package using a gradle task, as follows:

```shell
./gradlew completion -Pcp="/path/to/model.bin"
```

## Performance Data

The following data are all based on tests on Macbook Pro 2019 (2.3 GHz 8-core Intel Core i9). No rigorous performance testing has been done, so there may be fluctuations in actual operation.

### Llama2.c

Compile and test using `gcc -o run run.c -lm`. Data is as follows:

|          | parameters                                                                                 | tok/s |
|----------|--------------------------------------------------------------------------------------------|-------|
| Llama2.c | [stories15M.bin](https://huggingface.co/karpathy/tinyllamas/resolve/main/stories15M.bin)   | 26    |
| Llama2.c | [stories42M.bin](https://huggingface.co/karpathy/tinyllamas/resolve/main/stories42M.bin)   | 9     |
| Llama2.c | [stories110M.bin](https://huggingface.co/karpathy/tinyllamas/resolve/main/stories110M.bin) | 3     |

### Llama2.kt

|           | parameters                                                                                 | tok/s |
|-----------|--------------------------------------------------------------------------------------------|-------|
| Llama2.kt | [stories15M.bin](https://huggingface.co/karpathy/tinyllamas/resolve/main/stories15M.bin)   | 61    |
| Llama2.kt | [stories42M.bin](https://huggingface.co/karpathy/tinyllamas/resolve/main/stories42M.bin)   | 22    |
| Llama2.kt | [stories110M.bin](https://huggingface.co/karpathy/tinyllamas/resolve/main/stories110M.bin) | 8     |
