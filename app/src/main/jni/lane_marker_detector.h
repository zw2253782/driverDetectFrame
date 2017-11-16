//
// Created by lkang on 2/28/17.
//

#ifndef SELFDRIVING_LANE_MARKER_DETECTOR_H
#define SELFDRIVING_LANE_MARKER_DETECTOR_H


#include <iostream>
#include <vector>
#include <algorithm>
#include <math.h>
#include <time.h>
#include <map>

#include <opencv2/core.hpp>
#include <opencv2/core/utility.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/imgproc.hpp>

using namespace cv;
using namespace std;
const Vec3b kLaneWhite = Vec3b(255, 255, 255);
const Vec3b kLaneLightWhite = Vec3b(160, 160, 160);

const Vec3b kLaneYellow = Vec3b(150, 200, 255);
const Vec3b kLaneRed = Vec3b(0, 0, 255);

typedef vector<Point> Points;

class LaneMarkerDetector {

public:
	Mat& src_;
	vector<Points> left_lanes_;
	vector<Points> right_lanes_;

protected:
	bool isSameLane(Points& lane, Point& cur);
	Points getClosestLane(vector<Points>& lanes, Point center);

	double distToLaneColor(Vec3b& color);

public:
	LaneMarkerDetector(Mat& src);
	~LaneMarkerDetector();

	void addPoint(vector<Points>& lanes, Point cur);
	void laneMarkerDetector(Mat& img, Mat& src, Mat& temp);
	double colorDiff(const Vec3b& color, const Vec3b& another);
	void clear();

	Points getLeftLane(Point center);
	Points getRightLane(Point center);
	Points getDirectionLine();

};

#endif //SELFDRIVING_LANE_MARKER_DETECTOR_H
