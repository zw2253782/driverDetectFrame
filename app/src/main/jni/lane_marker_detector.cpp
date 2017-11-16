//
// Created by lkang on 2/28/17.
//

/*
 * lane_marker_detection.cpp
 *
 *  Created on: Feb 21, 2017
 *      Author: lkang
 */

#include "lane_marker_detector.h"


LaneMarkerDetector::LaneMarkerDetector(Mat& src): src_(src) {
}

LaneMarkerDetector::~LaneMarkerDetector() {

}

bool LaneMarkerDetector::isSameLane(Points& lane, Point& point) {
	int sz = lane.size();
	Point last = lane.at(sz - 1);
	double res = cv::norm(point - last);
	//cout<<point<<","<<last<<","<<res<<endl;
	if(res < 10.0) {
		return true;
	} else {
		return false;
	}
}
void LaneMarkerDetector::addPoint(vector<Points>& lanes, Point cur) {

	bool found = false;
	for(size_t i = 0; i < lanes.size(); ++i) {
		Points curPoints = lanes.at(i);
		if(isSameLane(curPoints, cur)) {
			lanes.at(i).push_back(cur);
			found = true;
			break;
		}
	}
	if(!found) {
		Points pts;
		pts.push_back(cur);
		lanes.push_back(pts);
	}
}

void LaneMarkerDetector::clear() {
	this->left_lanes_.clear();
	this->right_lanes_.clear();
}



Points LaneMarkerDetector::getClosestLane(vector<Points>& lanes, Point center) {
	int index = -1;
	double dist = 1000000;
	for(int i = 0; i < lanes.size(); ++i) {
		Points cur = lanes.at(i);
		if(cur.size() < 5) continue;
		double cur_dist = 1000000;
		for(int j = 0; j < cur.size(); ++j) {
			cur_dist = min(cur_dist, cv::norm(cur.at(j) - center));
		}
		if(cur_dist < dist) {
			dist = cur_dist;
			index = i;
		}
	}
	if(index == -1) {
		return Points();
	}
	return lanes.at(index);
}

Points LaneMarkerDetector::getLeftLane(Point center) {
	return this->getClosestLane(this->left_lanes_, center);
}
Points LaneMarkerDetector::getRightLane(Point center) {
	return this->getClosestLane(this->right_lanes_, center);
}

Points LaneMarkerDetector::getDirectionLine() {
	std::map<int,int> direction;
	Point center(this->src_.cols/2, this->src_.rows*4/5);
	Points left = this->getClosestLane(this->left_lanes_, center);
	Points right = this->getClosestLane(this->right_lanes_, center);

	for(int i = 0; i < left.size(); ++i) {
		Point point = left.at(i);
		int row = point.y;
		int col = point.x;
		direction[row] = col;
	}
	Points cline;
	for(int i = 0; i < right.size(); ++i) {
		Point point = right.at(i);
		int row = point.y;
		int col = point.x;
		if(direction.find(row) != direction.end()) {
			Point center = Point((col + direction[row])/2.0, row);
			cline.push_back(center);
		}
	}
	return cline;
}


double LaneMarkerDetector::colorDiff(const Vec3b& color, const Vec3b& another) {
	double sum = 0;
	for(int i = 0; i < 3; ++i) {
		sum += pow((int)color.val[i] - (int)another.val[i], 2.0);
	}
	double diff = sqrt(sum);
	return diff;
}

double LaneMarkerDetector::distToLaneColor(Vec3b& color) {
	double minDiff = numeric_limits<double>::max();
	minDiff = min(minDiff, colorDiff(color, kLaneYellow));
	minDiff = min(minDiff, colorDiff(color, kLaneWhite));
	minDiff = min(minDiff, colorDiff(color, kLaneLightWhite));
	return minDiff;
}

/**
 * img: the edge img calculated by canny edge detector
 * src: the source image
 * temp: a temp image used for testing
 */

void LaneMarkerDetector::laneMarkerDetector(Mat& img, Mat& src, Mat& temp)
{
	int cols = img.cols;
	int rows = img.rows;
	int start = rows * 1/4;
	int end = rows;

	//cout<<start<<","<<end<<endl;
	for(int x = 0; x < rows; ++x) {
		for(int y = 0; y < cols; ++y) {
			//black outside the are of interest
			if(x < start || x > end) {
				img.at<unsigned char>(x, y) = 0;
			} else {
				//draw a white line in the middle, as the forwarding direction of the car
				if(y == cols/2) {
					img.at<unsigned char>(x, y) = 255;
				}
			}
		}
	}

	//identify the lane markers, rows from bottom to top
	//columns from the middle to sides
	for(int x = end; x >= start; --x) {
		//left marker
		for(int i = cols/2 - 1; i >=0; --i) {
			unsigned char color = img.at<unsigned char>(x, i);
			if((int)color == 255) {
				double minDiff = 10000;

				for(int j = 0; j < 3; ++j) {
					Vec3b color = src.at<Vec3b>(x, i - j - 1);
					minDiff = distToLaneColor(color);
				}
				if(minDiff < 100) {
					temp.at<Vec3b>(x, i) = src.at<Vec3b>(x, i);
					this->addPoint(this->left_lanes_, Point(i, x));
				}
				break;
			}
		}
		//right marker
		for(int i = cols/2 + 1; i < cols; ++i) {
			unsigned char color = img.at<unsigned char>(x, i);
			if(color == 255) {
				double minDiff = 10000;
				for(int j = 0; j < 3; ++j) {
					Vec3b color = src.at<Vec3b>(x, i + j + 1);
					minDiff = distToLaneColor(color);
				}
				if(minDiff < 100) {
					temp.at<Vec3b>(x, i) = src.at<Vec3b>(x, i);
					this->addPoint(this->right_lanes_, Point(i, x));
				}
				break;
			}
		}
	}
}




