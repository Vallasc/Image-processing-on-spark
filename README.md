# Image denoising using a Spark cluster
This project aims to be a parallel and distributed implementation of the Gibbs denoising algorithm. Later, more algorithms for image processing based on the convolution method were added. Other implementations can be added extending the <code>Algorithm</code> trait and providing a pipeline for that.

The Gibbs sampling algorithm details are showed in the following [paper](http://stanford.edu/class/ee367/Winter2018/yue_ee367_win18_report.pdf).

### Processing pipeline
The program get the image that you want to process and split it into smaller chunks. Each sub-image, and its corresponding position, is pushed into a RDD and then processed by a Spark worker.

Multiple operations can be performed on the same chunk, these can be implemented exenting the <code>Pipeline</code> class and setting all the tasks needed. Tasks can also be implemented by extending the <code>Algorithm</code> trait.

By deafult these pipelines are implemented:
- **GibbsDenoiser**, applies a Gibbs denoiser task
- **MedianDenoiser**, applies two median filters
- **GibbsEdgeDetection**, applies a Gibbs denoiser task and the an edge detection kernel
- **EdgeDetection**, aplies a kernel to detect edges and then inverts th image

In the last stage al the processed sub-images are collected and the image is returned.

Example of a pipeline:
![alt text](./docs/pipeline.png)

### Make your own tasks and pipelines



### Run on a local Spark cluster
The program takes some parameters as input:
```bash
Usage: [--debug] [--padding] [--sub_matrix_size] [--pipeline] [--denoiser_runs] [--output_file_json] [--output_file_image] input_file_image
```

|Param|Description|
|-|-|
|--debug|Enable debug prints (enable by default = 1 ) |
|--padding|How many padding pixels to use when splitting the image|
|--sub_matrix_size|Size of sub-matrixes|
|--pipeline|Set the pipeline type|
|--denoiser_runs|Gibbs denoiser runs (only for pipelines that use this)|
|--output_file_json| Path to report file|
|--output_file_image|Path to output image|

Software requirements:
* sbt >= 1.6.1
* Spark >= 3.2.1

```bash
|> sbt assembly
|> spark-submit --driver-memory 8g --master local[*]  ./jar/binary.jar ./data/nike_noisy.png
```
If the program crashes on the collect phase, especially on big images, it is due to insufficient memory on the spark driver. You can change the driver memory settings using the param <code>--driver-memory</code>.


### Google Cloud setup


### Web interface