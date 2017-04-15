using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace genetic
{
    public class GenomeSample
    {
        public SortedList<Adress, double> genes = new SortedList<Adress, double>(new AdressComparer());
        public double fitness;
    }

    public class GeneticAlgorithm
    {
        public int generation = 0;
        public int pool_size = 300;
        public static double mutation_propability = 0.1f;
        public double swap_propabliity = 0.2f;
        double mutation_scale = 0.5f;
        internal List<Example> examples;
        internal NeuralNet net;
        internal SortedList<Adress, double> genome_base;
        internal List<GenomeSample> genome = new List<GenomeSample>();
        internal List<GenomeSample> newgenome = new List<GenomeSample>();
        Random r;
        GenomeSample RandomSample()
        {
            GenomeSample genome_sample = new GenomeSample();

            foreach (KeyValuePair<Adress, double> gene in genome_base)
            {
                genome_sample.genes.Add(gene.Key, Math.Pow((double)r.NextDouble(), 2)); //square distribution to make most initial weights very small

            }
            return genome_sample;
        }
        public void Initialise()
        {
            r = new Random();
            for (int i = 0; i < pool_size; i++)
            {

                genome.Add(RandomSample());
            }
            Calc_fitness();
        }
        public void Calc_fitness()
        {

            foreach (GenomeSample sample in genome)
            {
                double error = 0;
                net.weights.Clear();
                foreach (KeyValuePair<Adress, double> k in sample.genes)
                {
                    net.weights.Add(k.Key, k.Value);
                }
                foreach (Example example in examples)
                {

                    double[] results = net.PushTroughData(example.inputs);

                    for (int i = 0; i < example.outputs.Count(); i++)
                    {
                        error += Math.Pow(Math.Abs(example.outputs[i] - results[i]), 2) / example.outputs.Count();

                    }
                }
                sample.fitness = 1d / error;
            }

        }
        double SumOfFitness(List<GenomeSample> col)
        {
            double result = 0;
            foreach (GenomeSample s in col)
            {
                result += s.fitness;
            }
            return result;
        }
        int getIndexFromPieChart()
        {
            double rand = (double)r.NextDouble();
            int index = (int)((genome.Count - 1) * Math.Pow(rand, 5f));
            return index;
        }
        double Flip(double p1, double p2)
        {
            if (r.Next() % 2 == 0)
                return p1;
            return p2;
        }
        double getMutation(double p, bool iscrossover)
        {
            if (r.NextDouble() < mutation_propability)
            {
                p = p * (1 + ((double)r.NextDouble() - 0.5d) * mutation_scale);
              

                if (p > 1)
                {
                    p = 1;
                }
                if (p < 0)
                {
                    p = 0;
                }
            }
            return p;
        }
        void CreateNewGenomeSample()
        {
            GenomeSample g1 = genome[getIndexFromPieChart()];
            GenomeSample g2 = genome[getIndexFromPieChart()];

            GenomeSample g_new1 = new GenomeSample();
            int swappos = r.Next() % genome_base.Count;
            if (r.NextDouble() < swap_propabliity)
            {
                for (int i = 0; i < genome_base.Count; i++)
                {
                    g_new1.genes.Add(g1.genes.ElementAt(i).Key, getMutation(Flip(g1.genes.ElementAt(i).Value, g2.genes.ElementAt(i).Value), true));
                }
                newgenome.Add(g_new1);
            }
            else
            {
                for (int i = 0; i < genome_base.Count; i++)
                {
                    g_new1.genes.Add(g1.genes.ElementAt(i).Key, getMutation(g1.genes.ElementAt(i).Value, false));

                }
                newgenome.Add(g_new1);
            }
        }
        public void Step()
        {
            generation++;
            newgenome.Clear();
            genome.Sort(new GenomeSampleComparer());
            for (int i = 1; i < pool_size / 20; i++)
            {
                newgenome.Insert(0, RandomSample());
                                                   

            }
            while (newgenome.Count < pool_size)
            {
                CreateNewGenomeSample();
            }
            genome.AddRange(newgenome);
            Calc_fitness();
            genome.Sort(new GenomeSampleComparer());
            while (genome.Count > pool_size)
            {
                int pos = genome.Count - 1 - getIndexFromPieChart();
                genome.RemoveAt(pos);
            }

            Console.Out.WriteLine("gen" + generation + " fitness" + SumOfFitness(genome));

        }
    }
    public class Adress
    {
        public int layer;
        public int id;
        public int linkback_id;
        public Adress(int _layer, int _id, int _linkback_id)
        {
            layer = _layer;
            id = _id;
            linkback_id = _linkback_id;
        }
        public string toString()
        {
            return "layer " + layer + " id" + id + " link" + linkback_id;
        }
    }
    public class AdressComparer : IComparer<Adress>
    {
        public int Compare(Adress x, Adress y)
        {
            if (x.layer > y.layer)
            {
                return 1;
            }
            if (x.layer < y.layer)
            {
                return -1;
            }
            if (x.id > y.id)
            {
                return 1;
            }
            if (x.id < y.id)
            {
                return -1;
            }
            if (x.linkback_id > y.linkback_id)
            {
                return 1;
            }
            if (x.linkback_id < y.linkback_id)
            {
                return -1;
            }
            return 0;
        }
    }

    public class GenomeSampleComparer : IComparer<GenomeSample>
    {
        public int Compare(GenomeSample x, GenomeSample y)
        {
            if (x.fitness > y.fitness)
            {
                return -1;
            }
            if (x.fitness < y.fitness)
            {
                return 1;
            }
            return 0;
        }
    }
    public struct Example
    {
        public double[] inputs;
        public double[] outputs;
    }
    class Neuron
    {
        public double input;
        double scalar = 20d;
        public double output;
        public void activation()
        {
            output = 1 - (1 / (1 + (double)Math.Pow(Math.E, input * scalar)));

          //  output = (Math.Atan(input * scalar)/Math.PI)+0.5f;

        }
    }
    class NeuronLayer
    {
        public List<Neuron> neurons = new List<Neuron>();
        public NeuronLayer(int neuroncount)
        {
            for (int i = 0; i < neuroncount; i++)
            {
                Neuron n = new Neuron();
                neurons.Add(n);
            }
        }
    }
    public class NeuralNet
    {
        double bias_multiplier = 20f;
        NeuronLayer inputlayer;
        NeuronLayer outputlayer;
        List<NeuronLayer> middlelayers = new List<NeuronLayer>();
        List<NeuronLayer> layers = new List<NeuronLayer>();
        public GeneticAlgorithm training_algorithm;
        public SortedList<Adress, double> weights = new SortedList<Adress, double>(new AdressComparer());

        public NeuralNet(int[] dimensions)
        {
            CreateLayers(dimensions);
            CreateWeightsList();
            training_algorithm = new GeneticAlgorithm();
            training_algorithm.genome_base = new SortedList<Adress, double>(new AdressComparer());
            foreach (KeyValuePair<Adress, double> k in weights)
            {
                training_algorithm.genome_base.Add(k.Key, k.Value);
            }
            //  SetRandomWeights();
        }
        public void Train(List<Example> examples, int generations)
        {
            training_algorithm.examples = examples;
            training_algorithm.net = this;
            training_algorithm.Initialise();
            for (int i = 0; i < generations; i++)
            {
                training_algorithm.Step();
            }
        }
        public double[] PushTroughData(double[] input)
        {
            for (int i = 0; i < inputlayer.neurons.Count; i++)
            {
                inputlayer.neurons[i].input = input[i];
                inputlayer.neurons[i].activation();
            }
            for (int i = 1; i < layers.Count; i++)
            {
                foreach (Neuron k in layers[i].neurons)
                {
                    double sum = 0;
                    for (int j = 0; j < layers[i - 1].neurons.Count; j++)
                    {
                        double w = 0;
                        weights.TryGetValue(new Adress(i, layers[i].neurons.IndexOf(k), j), out w);
                        sum += (w - 0.5f) * 2 * layers[i - 1].neurons[j].output;
                    }
                    double w2 = 0;
                    weights.TryGetValue(new Adress(i, layers[i].neurons.IndexOf(k), -1), out w2);
                    sum += (w2 - 0.5d) * bias_multiplier;
                    k.input = sum;
                    k.activation();

                }
            }


            double[] output = new double[outputlayer.neurons.Count];
            for (int i = 0; i < outputlayer.neurons.Count; i++)
            {
                output[i] = outputlayer.neurons[i].output;
            }

            return output;
        }
        void CreateLayers(int[] dimensions)
        {
            inputlayer = new NeuronLayer(dimensions[0]);
            for (int i = 1; i < dimensions.Count() - 1; i++)
            {
                NeuronLayer n = new NeuronLayer(dimensions[i]);
                middlelayers.Add(n);
            }
            outputlayer = new NeuronLayer(dimensions.Last());
            MergeLayers();
        }
        void MergeLayers()
        {
            layers.Clear();
            layers.Add(inputlayer);
            layers.AddRange(middlelayers);
            layers.Add(outputlayer);
        }
        void CreateWeightsList()
        {
            for (int i = 1; i < layers.Count; i++)//warstwa
            {
                for (int j = 0; j < layers[i].neurons.Count; j++)//neuron w warstwie
                {
                    for (int k = -1; k < layers[i - 1].neurons.Count; k++) // neuron w warstwie poprzedniej
                    {
                        weights.Add(new Adress(i, j, k), 0);
                    }

                }
            }
        }

        internal SortedList<Adress, double> GetWeights()
        {
            return training_algorithm.genome[0].genes;
        }
    }

    class Program
    {
        static void Main(string[] args)
        {
            int[] net_dims = { 2, 3, 1 };
            double[] data = { 0.0f, 1.0f };
            List<Example> training_data = new List<Example>();
            for (int i = 0; i < 4; i++) // XOR
            {
                Example p = new Example();
                p.inputs = new double[2];
                p.outputs = new double[1];
                p.inputs[0] = i % 2;
                p.inputs[1] = i / 2;
                p.outputs[0] = 1;
                if (p.inputs[0] == p.inputs[1])
                {
                    p.outputs[0] = 0;
                }
                training_data.Add(p);
            }
            

            NeuralNet net = new NeuralNet(net_dims);
            net.Train(training_data, 1000);
            net.GetWeights();
            foreach (Example ex in training_data)
            {

                net.PushTroughData(ex.inputs).ToList().ForEach(Console.WriteLine);
                ex.outputs.ToList().ForEach(Console.WriteLine);
            }
            Console.ReadKey();
        }
    }
}
