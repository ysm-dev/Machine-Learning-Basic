#include <stdio.h>
#include <math.h>
#include <glut.h>
#include <time.h>
#include <stdlib.h>
#include <gl\GLU.h>
#include <Windows.h>

// Define constant
#define RNDMAX 32767.0
#define SCREEN_WID 750
#define SCREEN_HEI 500
#define IN 2
#define OUT 1
#define TRUE 1
#define FALSE 0
#define MAX 100
#define graph_x 0.0001
#define RESET 6000
#define PIE 3.141592

// Type definition
typedef struct {
	float x[IN];
	float y[OUT];
}Training_Set;

typedef struct {
	float W[MAX][MAX];
	float b[MAX];
}Neuron;
// Define global structure
Training_Set *data;
Neuron N[MAX];

// Define global variable
int draw;
int print;
int n_data;
int Time;
int next;
static int delay = 1;

float cluster1[2];
float cluster2[2];

float learning_rate = 1.0;
float dropout_rate;
float regular_rate;

float cost;
float accuracy;

float draw_x = 0.0;
float cost_prev = 0.0;
float accuracy_prev = 0.0;

float RND(float min, float max) {
	return (rand() / RNDMAX)*(max - min) + min;
}

float sigmoid(float i) {
	return 1.0 / (1.0 + exp(-i));
}

float L0(float *i) {
	return N[0].W[0][0] * i[0] + N[0].W[1][0] * i[1] + N[0].b[0];
}

float hypothesis(float *i) {
	return L0(i);
}

float sig_hypothesis(float *i) {
	return sigmoid(hypothesis(i));
}

void train(float rate) {
	float sumW = 0;
	float sumb = 0;

	for (int i = 0; i < IN; i++) {
		sumW = 0;
		for (int j = 0; j < n_data; j++) {
			sumW += (sig_hypothesis(data[j].x) - data[j].y[0])*data[j].x[i];
		}
		N[0].W[i][0] = N[0].W[i][0] - rate*(1.0 / n_data)*sumW;
	}

	for (int i = 0; i < n_data; i++) {
		sumb += (sig_hypothesis(data[i].x) - data[i].y[0]);
	}

	N[0].b[0] = N[0].b[0] - rate*(1.0 / n_data)*sumb;

}

float calculate_cost() {
	cost = 0;

	for (int i = 0; i < n_data; i++) {
		cost += data[i].y[0] * log(sig_hypothesis(data[i].x)) + (1.0 - data[i].y[0]) * log(1.0 - sig_hypothesis(data[i].x));
	}

	return -cost / n_data;
}

float calculate_accuracy() {
	int sum = 0;

	for (int i = 0; i < n_data; i++) {
		if (sig_hypothesis(data[i].x) > 0.5 && data[i].y[0] == 1) {
			sum++;
		}
		else if (sig_hypothesis(data[i].x) <= 0.5 && data[i].y[0] == 0) {
			sum++;
		}
	}

	return (float)sum / n_data;
}

void read_file() {

	freopen("Testset1.txt", "r", stdin);

	scanf("%d", &n_data);

	for (int i = 0; i < n_data; i++) {
		for (int j = 0; j < IN; j++)
			scanf("%f", &data[i].x[j]);
		for (int j = 0; j < OUT; j++)
			scanf("%f", &data[i].y[j]);
	}

}

void init_variable() {

	n_data = 720;
	Time = 0;
	draw = FALSE;
	print = FALSE;
	next = 0;

	data = (Training_Set*)malloc(sizeof(Training_Set)*n_data);

	cluster1[0] = 0.3;
	cluster1[1] = 0.6;
	cluster2[0] = 0.7;
	cluster2[1] = 0.3;

	// Initialize random x, y, w, b
	for (int i = 0; i < n_data/2; i++){
		data[i].x[0] = cluster1[0] + RND(0.0, 0.3) * cos(i*PIE / 180);
		data[i].x[1] = cluster1[1] + RND(0.0, 0.3) * sin(i*PIE / 180);
	}

	for (int i = n_data/2; i < n_data; i++) {
		data[i].x[0] = cluster2[0] + RND(0.0, 0.3) * cos(i*PIE / 180);
		data[i].x[1] = cluster2[1] + RND(0.0, 0.3) * sin(i*PIE / 180);
	}

	for (int i = 0; i < n_data/2; i++)
		for (int j = 0; j < OUT; j++)
			data[i].y[j] = 0.0;

	for (int i = n_data/2; i < n_data; i++)
		for (int j = 0; j < OUT; j++)
			data[i].y[j] = 1.0;

	for (int i = 0; i < IN; i++)
		for (int j = 0; j < OUT; j++)
			N[0].W[i][j] = RND(-1, 1);

	for (int i = 0; i < OUT; i++)
		N[0].b[i] = RND(-1, 1);

}

void print_variable() {

	// Print testset data
	printf("x1   x2   | y\n");

	for (int i = 0; i < n_data; i++) {
		for (int j = 0; j < IN; j++) {
			printf("%.1f ", data[i].x[j]);
		}
		printf("| ");
		for (int j = 0; j < OUT; j++) {
			printf("%.1f ", data[i].y[j]);
		}
		printf("\n");
	}

}

void init() {
	srand(time(0));

	//read_file();
	init_variable();
	//print_variable();

	glClearColor(1.0, 1.0, 1.0, 1.0);
	glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	glMatrixMode(GL_PROJECTION);
	glLoadIdentity();
}

void draw_axis() {

	// Draw x, y axis
	glColor3f(0.0, 0.0, 0.0);

	glBegin(GL_LINES);
	glVertex3f(-MAX, 0.0, 0.0);
	glVertex3f(MAX, 0.0, 0.0);
	glEnd();

	glBegin(GL_LINES);
	glVertex3f(0.0, -MAX, 0.0);
	glVertex3f(0.0, MAX, 0.0);
	glEnd();

}

void draw_data() {

	// Draw data
	glPointSize(4);
	glBegin(GL_POINTS);
	for (int i = 0; i < n_data; i++) {
		if (data[i].y[0] == 0.0) glColor3f(1.0, 0.0, 0.0);
		else glColor3f(0.0, 0.0, 1.0);
		glVertex3f(data[i].x[0], data[i].x[1], 0.0);
	}
	glEnd();

}

void accuracy_graph() {

	// Draw accuracy graph
	glColor3f(0.0, 0.0, 0.0);
	if (Time > 1) {
		glBegin(GL_LINES);
		glVertex3f(draw_x, accuracy_prev, 0.1);
		glVertex3f(draw_x + graph_x, accuracy, 0.1);
		glEnd();
	}

}

void cost_graph() {

	// Draw cost graph
	glLineWidth(2);
	glColor3f(0.0, 0.0, 0.0);
	if (Time > 1 && cost < 1.0) {
		glBegin(GL_LINES);
		glVertex3f(draw_x, cost_prev, 0.1);
		glVertex3f(draw_x + graph_x, cost, 0.1);
		glEnd();
	}

}

void draw_graph() {

	// Draw x, y axis for graph
	glColor3f(0.0, 0.0, 0.0);

	glBegin(GL_LINES);
	glVertex3f(-0.1, -0.1, 0.0);
	glVertex3f(1.1, -0.1, 0.0);
	glEnd();

	glBegin(GL_LINES);
	glVertex3f(-0.1, -0.1, 0.0);
	glVertex3f(-0.1, 1.1, 0.0);
	glEnd();

	// Draw graph scale

	for (float i = -0.1; i < 1.1; i += 0.1) {
		if (i < 0.01 || i == 0.5 || i > 0.99) glColor3f(0.85, 0.85, 0.85);
		else glColor3f(0.93, 0.93, 0.93);
		glBegin(GL_LINES);
		glVertex3f(-0.1, i, 0.0);
		glVertex3f(1.1, i, 0.0);
		glEnd();
		glBegin(GL_LINES);
		glVertex3f(i, -0.1, 0.0);
		glVertex3f(i, 1.1, 0.0);
		glEnd();
	}

}


void draw_test() {

	if (draw == TRUE) {
		for (int i = 0; i < 100; i++) {
			float test[2];
			test[0] = RND(-1.0, 1.0);
			test[1] = RND(-1.0, 1.0);
			glPointSize(4);
			glBegin(GL_POINTS);
			if (sig_hypothesis(test) < 0.5)  glColor3f(1.0, 0.5, 0.5);
			else  glColor3f(0.5, 0.5, 1.0);
			glVertex3f(test[0], test[1], 0.0);
			glEnd();
		}
	}

}

void viewport1() {

	glLoadIdentity();
	glScissor(0, 0, SCREEN_WID * 2.0 / 3.0, SCREEN_HEI);
	glViewport(0, 0, SCREEN_WID * 2.0 / 3.0, SCREEN_HEI);
	glOrtho(0.0, 1.0, 0.0, 1.0, -1.0, 1.0);

	if (Time % RESET == 0) glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	// x축 2등분 분할
	glColor3f(0.0, 0.0, 0.0);
	glBegin(GL_LINES);
	glVertex3f(1.0, -MAX, 0.0);
	glVertex3f(1.0, MAX, 0.0);
	glEnd();

	//draw_graph();
	if (Time % RESET == 0) draw_data();
	if (Time % RESET == 3500) draw = TRUE;

	draw_test();

	glEnd();

}

void viewport2() {

	glLoadIdentity();
	glScissor(SCREEN_WID * 2 / 3, SCREEN_HEI / 2, SCREEN_WID / 3, SCREEN_HEI / 2);
	glViewport(SCREEN_WID * 2 / 3, SCREEN_HEI / 2, SCREEN_WID / 3, SCREEN_HEI / 2);
	glOrtho(-0.2, 1.2, -0.2, 1.2, -1.0, 1.0);
	if (Time % RESET == 2) glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

	draw_graph();

	cost_graph();

}

void viewport3() {

	glLoadIdentity();
	glScissor(SCREEN_WID * 2 / 3, 0, SCREEN_WID / 3, SCREEN_HEI / 2);
	glViewport(SCREEN_WID * 2 / 3, 0, SCREEN_WID / 3, SCREEN_HEI / 2);
	glOrtho(-0.2, 1.2, -0.2, 1.2, -1.0, 1.0);
	if (Time % RESET == 2) glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
	draw_graph();

	accuracy_graph();

}

void random_draw() {

	if (Time % RESET == 0) {
		next = ++next % 3;
		draw_x = 0.0;
		
		float r = RND(0.3, 0.5);
		draw = FALSE;

		if (next == 0) {
			cluster1[0] = 0.3;
			cluster1[1] = 0.6;
			cluster2[0] = 0.7;
			cluster2[1] = 0.3;
		}
		else if (next == 1) {
			cluster1[0] = 0.7;
			cluster1[1] = 0.3;
			cluster2[0] = 0.3;
			cluster2[1] = 0.4;
		}
		else if (next == 2) {
			cluster1[0] = 0.3;
			cluster1[1] = 0.3;
			cluster2[0] = 0.7;
			cluster2[1] = 0.7;
		}

		// Initialize random x, y, w, b
		for (int i = 0; i < n_data / 2; i++) {
			data[i].x[0] = cluster1[0] + RND(0.0, r) * cos(i*PIE / 180);
			data[i].x[1] = cluster1[1] + RND(0.0, r) * sin(i*PIE / 180);
		}

		for (int i = n_data / 2; i < n_data; i++) {
			data[i].x[0] = cluster2[0] + RND(0.0, r) * cos(i*PIE / 180);
			data[i].x[1] = cluster2[1] + RND(0.0, r) * sin(i*PIE / 180);
		}
	}

}

void display() {

	if (Time > 1) {
		accuracy_prev = accuracy;
		cost_prev = cost;
	}

	accuracy = calculate_accuracy();
	cost = calculate_cost();
	train(learning_rate);
	random_draw();

	glEnable(GL_DEPTH_TEST);
	glMatrixMode(GL_MODELVIEW);
	glEnable(GL_SCISSOR_TEST);


	viewport1();
	viewport2();
	viewport3();

	if (Time % 1000 == 0)
		printf("[ %.3lf ][ %.3lf ]\n", cost, accuracy);

	glFlush();
	draw_x += graph_x;
	Time++;
}

void keyboard_handler(unsigned char key, int x, int y) {
	if (key == 'd') {
		draw = TRUE;
	}
	if (key == 'r') {
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		draw = 0;
		draw_x = 0;
	}
	if (key == 'p') {
		print = TRUE;
	}
}

void timer(int t) {
	glutPostRedisplay();
	glutTimerFunc(delay, timer, t);
}

int main(int argc, char* argv[]) {

	glutInit(&argc, (char**)argv);
	glutInitDisplayMode(GLUT_SINGLE | GLUT_RGB | GLUT_DEPTH);
	glutInitWindowSize(SCREEN_WID, SCREEN_HEI);
	glutInitWindowPosition(400, 0);
	glutCreateWindow("Classification");
	glutKeyboardFunc(keyboard_handler);
	glutTimerFunc(delay, timer, 0);
	glutDisplayFunc(display);


	init();
	glutMainLoop();


	return 0;
}