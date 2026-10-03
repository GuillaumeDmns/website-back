package com.gdamiens.website.controller.object.v2;

import java.util.List;

public record LineDetail(LineSummary line, List<LineDirection> directions) {
}
