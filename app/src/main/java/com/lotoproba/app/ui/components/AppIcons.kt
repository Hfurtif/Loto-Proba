package com.lotoproba.app.ui.components

import androidx.compose.material.icons.materialIcon
import androidx.compose.material.icons.materialPath
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Icônes vectorielles légères remplaçant material-icons-extended :
 * seules les icônes réellement utilisées par LotoProba sont compilées.
 */
object AppIcons {

    val AddCircle: ImageVector by lazy {
        materialIcon(name = "App.AddCircle") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(17.0f, 13.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
    }

    val Analytics: ImageVector by lazy {
        materialIcon(name = "App.Analytics") {
            materialPath {
                moveTo(19.0f, 3.0f)
                horizontalLineTo(5.0f)
                curveTo(3.9f, 3.0f, 3.0f, 3.9f, 3.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveTo(21.0f, 3.9f, 20.1f, 3.0f, 19.0f, 3.0f)
                close()
                moveTo(9.0f, 17.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(17.0f)
                close()
                moveTo(13.0f, 17.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(17.0f)
                close()
                moveTo(13.0f, 12.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(12.0f)
                close()
                moveTo(17.0f, 17.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(17.0f)
                close()
            }
        }
    }

    val AutoAwesome: ImageVector by lazy {
        materialIcon(name = "App.AutoAwesome") {
            materialPath {
                moveTo(19.0f, 9.0f)
                lineToRelative(1.25f, -2.75f)
                lineTo(23.0f, 5.0f)
                lineToRelative(-2.75f, -1.25f)
                lineTo(19.0f, 1.0f)
                lineToRelative(-1.25f, 2.75f)
                lineTo(15.0f, 5.0f)
                lineToRelative(2.75f, 1.25f)
                lineTo(19.0f, 9.0f)
                close()
                moveTo(11.5f, 9.5f)
                lineTo(9.0f, 4.0f)
                lineTo(6.5f, 9.5f)
                lineTo(1.0f, 12.0f)
                lineToRelative(5.5f, 2.5f)
                lineTo(9.0f, 20.0f)
                lineToRelative(2.5f, -5.5f)
                lineTo(17.0f, 12.0f)
                lineTo(11.5f, 9.5f)
                close()
                moveTo(19.0f, 15.0f)
                lineToRelative(-1.25f, 2.75f)
                lineTo(15.0f, 19.0f)
                lineToRelative(2.75f, 1.25f)
                lineTo(19.0f, 23.0f)
                lineToRelative(1.25f, -2.75f)
                lineTo(23.0f, 19.0f)
                lineToRelative(-2.75f, -1.25f)
                lineTo(19.0f, 15.0f)
                close()
            }
        }
    }

    val AutoGraph: ImageVector by lazy {
        materialIcon(name = "App.AutoGraph") {
            materialPath {
                moveTo(14.06f, 9.94f)
                lineTo(12.0f, 9.0f)
                lineToRelative(2.06f, -0.94f)
                lineTo(15.0f, 6.0f)
                lineToRelative(0.94f, 2.06f)
                lineTo(18.0f, 9.0f)
                lineToRelative(-2.06f, 0.94f)
                lineTo(15.0f, 12.0f)
                lineTo(14.06f, 9.94f)
                close()
                moveTo(4.0f, 14.0f)
                lineToRelative(0.94f, -2.06f)
                lineTo(7.0f, 11.0f)
                lineToRelative(-2.06f, -0.94f)
                lineTo(4.0f, 8.0f)
                lineToRelative(-0.94f, 2.06f)
                lineTo(1.0f, 11.0f)
                lineToRelative(2.06f, 0.94f)
                lineTo(4.0f, 14.0f)
                close()
                moveTo(8.5f, 9.0f)
                lineToRelative(1.09f, -2.41f)
                lineTo(12.0f, 5.5f)
                lineTo(9.59f, 4.41f)
                lineTo(8.5f, 2.0f)
                lineTo(7.41f, 4.41f)
                lineTo(5.0f, 5.5f)
                lineToRelative(2.41f, 1.09f)
                lineTo(8.5f, 9.0f)
                close()
                moveTo(4.5f, 20.5f)
                lineToRelative(6.0f, -6.01f)
                lineToRelative(4.0f, 4.0f)
                lineTo(23.0f, 8.93f)
                lineToRelative(-1.41f, -1.41f)
                lineToRelative(-7.09f, 7.97f)
                lineToRelative(-4.0f, -4.0f)
                lineTo(3.0f, 19.0f)
                lineTo(4.5f, 20.5f)
                close()
            }
        }
    }

    val Balance: ImageVector by lazy {
        materialIcon(name = "App.Balance") {
            materialPath {
                moveTo(13.0f, 7.83f)
                curveToRelative(0.85f, -0.3f, 1.53f, -0.98f, 1.83f, -1.83f)
                horizontalLineTo(18.0f)
                lineToRelative(-3.0f, 7.0f)
                curveToRelative(0.0f, 1.66f, 1.57f, 3.0f, 3.5f, 3.0f)
                reflectiveCurveToRelative(3.5f, -1.34f, 3.5f, -3.0f)
                lineToRelative(-3.0f, -7.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(-6.17f)
                curveTo(14.42f, 2.83f, 13.31f, 2.0f, 12.0f, 2.0f)
                reflectiveCurveTo(9.58f, 2.83f, 9.17f, 4.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                lineToRelative(-3.0f, 7.0f)
                curveToRelative(0.0f, 1.66f, 1.57f, 3.0f, 3.5f, 3.0f)
                reflectiveCurveTo(9.0f, 14.66f, 9.0f, 13.0f)
                lineTo(6.0f, 6.0f)
                horizontalLineToRelative(3.17f)
                curveToRelative(0.3f, 0.85f, 0.98f, 1.53f, 1.83f, 1.83f)
                verticalLineTo(19.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(20.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-9.0f)
                verticalLineTo(7.83f)
                close()
                moveTo(20.37f, 13.0f)
                horizontalLineToRelative(-3.74f)
                lineToRelative(1.87f, -4.36f)
                lineTo(20.37f, 13.0f)
                close()
                moveTo(7.37f, 13.0f)
                horizontalLineTo(3.63f)
                lineTo(5.5f, 8.64f)
                lineTo(7.37f, 13.0f)
                close()
                moveTo(12.0f, 6.0f)
                curveToRelative(-0.55f, 0.0f, -1.0f, -0.45f, -1.0f, -1.0f)
                curveToRelative(0.0f, -0.55f, 0.45f, -1.0f, 1.0f, -1.0f)
                reflectiveCurveToRelative(1.0f, 0.45f, 1.0f, 1.0f)
                curveTo(13.0f, 5.55f, 12.55f, 6.0f, 12.0f, 6.0f)
                close()
            }
        }
    }

    val BookmarkAdd: ImageVector by lazy {
        materialIcon(name = "App.BookmarkAdd") {
            materialPath {
                moveTo(21.0f, 7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                close()
                moveTo(19.0f, 21.0f)
                lineToRelative(-7.0f, -3.0f)
                lineToRelative(-7.0f, 3.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, 0.9f, -2.0f, 2.0f, -2.0f)
                horizontalLineToRelative(7.0f)
                curveToRelative(-0.63f, 0.84f, -1.0f, 1.87f, -1.0f, 3.0f)
                curveToRelative(0.0f, 2.76f, 2.24f, 5.0f, 5.0f, 5.0f)
                curveToRelative(0.34f, 0.0f, 0.68f, -0.03f, 1.0f, -0.1f)
                verticalLineTo(21.0f)
                close()
            }
        }
    }

    val CalendarMonth: ImageVector by lazy {
        materialIcon(name = "App.CalendarMonth") {
            materialPath {
                moveTo(19.0f, 4.0f)
                horizontalLineToRelative(-1.0f)
                verticalLineTo(2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(2.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(5.0f)
                curveTo(3.89f, 4.0f, 3.0f, 4.9f, 3.0f, 6.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(6.0f)
                curveTo(21.0f, 4.9f, 20.1f, 4.0f, 19.0f, 4.0f)
                close()
                moveTo(19.0f, 20.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(14.0f)
                verticalLineTo(20.0f)
                close()
                moveTo(9.0f, 14.0f)
                horizontalLineTo(7.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(13.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
                moveTo(17.0f, 14.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(14.0f)
                close()
            }
        }
    }

    val Casino: ImageVector by lazy {
        materialIcon(name = "App.Casino") {
            materialPath {
                moveTo(19.0f, 3.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(7.5f, 18.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(6.67f, 15.0f, 7.5f, 15.0f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveTo(8.33f, 18.0f, 7.5f, 18.0f)
                close()
                moveTo(7.5f, 9.0f)
                curveTo(6.67f, 9.0f, 6.0f, 8.33f, 6.0f, 7.5f)
                reflectiveCurveTo(6.67f, 6.0f, 7.5f, 6.0f)
                reflectiveCurveTo(9.0f, 6.67f, 9.0f, 7.5f)
                reflectiveCurveTo(8.33f, 9.0f, 7.5f, 9.0f)
                close()
                moveTo(12.0f, 13.5f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(16.5f, 18.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveToRelative(0.67f, -1.5f, 1.5f, -1.5f)
                reflectiveCurveToRelative(1.5f, 0.67f, 1.5f, 1.5f)
                reflectiveCurveToRelative(-0.67f, 1.5f, -1.5f, 1.5f)
                close()
                moveTo(16.5f, 9.0f)
                curveToRelative(-0.83f, 0.0f, -1.5f, -0.67f, -1.5f, -1.5f)
                reflectiveCurveTo(15.67f, 6.0f, 16.5f, 6.0f)
                reflectiveCurveTo(18.0f, 6.67f, 18.0f, 7.5f)
                reflectiveCurveTo(17.33f, 9.0f, 16.5f, 9.0f)
                close()
            }
        }
    }

    val ChevronLeft: ImageVector by lazy {
        materialIcon(name = "App.ChevronLeft") {
            materialPath {
                moveTo(15.41f, 7.41f)
                lineTo(14.0f, 6.0f)
                lineToRelative(-6.0f, 6.0f)
                lineToRelative(6.0f, 6.0f)
                lineToRelative(1.41f, -1.41f)
                lineTo(10.83f, 12.0f)
                close()
            }
        }
    }

    val ChevronRight: ImageVector by lazy {
        materialIcon(name = "App.ChevronRight") {
            materialPath {
                moveTo(10.0f, 6.0f)
                lineTo(8.59f, 7.41f)
                lineTo(13.17f, 12.0f)
                lineToRelative(-4.58f, 4.59f)
                lineTo(10.0f, 18.0f)
                lineToRelative(6.0f, -6.0f)
                close()
            }
        }
    }

    val ContentCopy: ImageVector by lazy {
        materialIcon(name = "App.ContentCopy") {
            materialPath {
                moveTo(16.0f, 1.0f)
                horizontalLineTo(4.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(12.0f)
                verticalLineTo(1.0f)
                close()
                moveTo(19.0f, 5.0f)
                horizontalLineTo(8.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(11.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(7.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(19.0f, 21.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(11.0f)
                verticalLineToRelative(14.0f)
                close()
            }
        }
    }

    val ContentPaste: ImageVector by lazy {
        materialIcon(name = "App.ContentPaste") {
            materialPath {
                moveTo(19.0f, 2.0f)
                horizontalLineToRelative(-4.18f)
                curveTo(14.4f, 0.84f, 13.3f, 0.0f, 12.0f, 0.0f)
                curveToRelative(-1.3f, 0.0f, -2.4f, 0.84f, -2.82f, 2.0f)
                horizontalLineTo(5.0f)
                curveToRelative(-1.1f, 0.0f, -2.0f, 0.9f, -2.0f, 2.0f)
                verticalLineToRelative(16.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(14.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.0f)
                curveToRelative(0.0f, -1.1f, -0.9f, -2.0f, -2.0f, -2.0f)
                close()
                moveTo(12.0f, 2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, 0.45f, 1.0f, 1.0f)
                reflectiveCurveToRelative(-0.45f, 1.0f, -1.0f, 1.0f)
                reflectiveCurveToRelative(-1.0f, -0.45f, -1.0f, -1.0f)
                reflectiveCurveToRelative(0.45f, -1.0f, 1.0f, -1.0f)
                close()
                moveTo(19.0f, 20.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(10.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(16.0f)
                close()
            }
        }
    }

    val DeleteSweep: ImageVector by lazy {
        materialIcon(name = "App.DeleteSweep") {
            materialPath {
                moveTo(15.0f, 16.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-4.0f)
                close()
                moveTo(15.0f, 8.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-7.0f)
                close()
                moveTo(15.0f, 12.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-6.0f)
                close()
                moveTo(3.0f, 18.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(3.0f)
                verticalLineTo(18.0f)
                close()
                moveTo(14.0f, 5.0f)
                horizontalLineToRelative(-3.0f)
                lineToRelative(-1.0f, -1.0f)
                horizontalLineTo(6.0f)
                lineTo(5.0f, 5.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(12.0f)
                close()
            }
        }
    }

    val Download: ImageVector by lazy {
        materialIcon(name = "App.Download") {
            materialPath {
                moveTo(5.0f, 20.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(20.0f)
                close()
                moveTo(19.0f, 9.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(3.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(6.0f)
                horizontalLineTo(5.0f)
                lineToRelative(7.0f, 7.0f)
                lineTo(19.0f, 9.0f)
                close()
            }
        }
    }

    val ExpandLess: ImageVector by lazy {
        materialIcon(name = "App.ExpandLess") {
            materialPath {
                moveTo(12.0f, 8.0f)
                lineToRelative(-6.0f, 6.0f)
                lineToRelative(1.41f, 1.41f)
                lineTo(12.0f, 10.83f)
                lineToRelative(4.59f, 4.58f)
                lineTo(18.0f, 14.0f)
                close()
            }
        }
    }

    val ExpandMore: ImageVector by lazy {
        materialIcon(name = "App.ExpandMore") {
            materialPath {
                moveTo(16.59f, 8.59f)
                lineTo(12.0f, 13.17f)
                lineTo(7.41f, 8.59f)
                lineTo(6.0f, 10.0f)
                lineToRelative(6.0f, 6.0f)
                lineToRelative(6.0f, -6.0f)
                close()
            }
        }
    }

    val FactCheck: ImageVector by lazy {
        materialIcon(name = "App.FactCheck") {
            materialPath {
                moveTo(20.0f, 3.0f)
                horizontalLineTo(4.0f)
                curveTo(2.9f, 3.0f, 2.0f, 3.9f, 2.0f, 5.0f)
                verticalLineToRelative(14.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(16.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(5.0f)
                curveTo(22.0f, 3.9f, 21.1f, 3.0f, 20.0f, 3.0f)
                close()
                moveTo(10.0f, 17.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(17.0f)
                close()
                moveTo(10.0f, 13.0f)
                horizontalLineTo(5.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(13.0f)
                close()
                moveTo(10.0f, 9.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(9.0f)
                close()
                moveTo(14.82f, 15.0f)
                lineTo(12.0f, 12.16f)
                lineToRelative(1.41f, -1.41f)
                lineToRelative(1.41f, 1.42f)
                lineTo(17.99f, 9.0f)
                lineToRelative(1.42f, 1.42f)
                lineTo(14.82f, 15.0f)
                close()
            }
        }
    }

    val FormatListNumbered: ImageVector by lazy {
        materialIcon(name = "App.FormatListNumbered") {
            materialPath {
                moveTo(2.0f, 17.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(0.5f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(0.5f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-4.0f)
                horizontalLineTo(2.0f)
                verticalLineTo(17.0f)
                close()
                moveTo(3.0f, 8.0f)
                horizontalLineToRelative(1.0f)
                verticalLineTo(4.0f)
                horizontalLineTo(2.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(1.0f)
                verticalLineTo(8.0f)
                close()
                moveTo(2.0f, 11.0f)
                horizontalLineToRelative(1.8f)
                lineTo(2.0f, 13.1f)
                verticalLineToRelative(0.9f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineTo(3.2f)
                lineTo(5.0f, 10.9f)
                verticalLineTo(10.0f)
                horizontalLineTo(2.0f)
                verticalLineTo(11.0f)
                close()
                moveTo(7.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(14.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(7.0f)
                close()
                moveTo(7.0f, 19.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(19.0f)
                close()
                moveTo(7.0f, 13.0f)
                horizontalLineToRelative(14.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(7.0f)
                verticalLineTo(13.0f)
                close()
            }
        }
    }

    val Functions: ImageVector by lazy {
        materialIcon(name = "App.Functions") {
            materialPath {
                moveTo(18.0f, 4.0f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(2.0f)
                lineToRelative(6.5f, 6.0f)
                lineTo(6.0f, 18.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineToRelative(-7.0f)
                lineToRelative(5.0f, -5.0f)
                lineToRelative(-5.0f, -5.0f)
                horizontalLineToRelative(7.0f)
                close()
            }
        }
    }

    val HourglassEmpty: ImageVector by lazy {
        materialIcon(name = "App.HourglassEmpty") {
            materialPath {
                moveTo(6.0f, 2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(0.01f)
                lineTo(6.0f, 8.01f)
                lineTo(10.0f, 12.0f)
                lineToRelative(-4.0f, 4.0f)
                lineToRelative(0.01f, 0.01f)
                horizontalLineTo(6.0f)
                verticalLineTo(22.0f)
                horizontalLineToRelative(12.0f)
                verticalLineToRelative(-5.99f)
                horizontalLineToRelative(-0.01f)
                lineTo(18.0f, 16.0f)
                lineToRelative(-4.0f, -4.0f)
                lineToRelative(4.0f, -3.99f)
                lineToRelative(-0.01f, -0.01f)
                horizontalLineTo(18.0f)
                verticalLineTo(2.0f)
                horizontalLineTo(6.0f)
                close()
                moveTo(16.0f, 16.5f)
                verticalLineTo(20.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-3.5f)
                lineToRelative(4.0f, -4.0f)
                lineToRelative(4.0f, 4.0f)
                close()
                moveTo(12.0f, 11.5f)
                lineToRelative(-4.0f, -4.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(3.5f)
                lineToRelative(-4.0f, 4.0f)
                close()
            }
        }
    }

    val Key: ImageVector by lazy {
        materialIcon(name = "App.Key") {
            materialPath {
                moveTo(21.0f, 10.0f)
                horizontalLineToRelative(-8.35f)
                curveTo(11.83f, 7.67f, 9.61f, 6.0f, 7.0f, 6.0f)
                curveToRelative(-3.31f, 0.0f, -6.0f, 2.69f, -6.0f, 6.0f)
                reflectiveCurveToRelative(2.69f, 6.0f, 6.0f, 6.0f)
                curveToRelative(2.61f, 0.0f, 4.83f, -1.67f, 5.65f, -4.0f)
                horizontalLineTo(13.0f)
                lineToRelative(2.0f, 2.0f)
                lineToRelative(2.0f, -2.0f)
                lineToRelative(2.0f, 2.0f)
                lineToRelative(4.0f, -4.0f)
                lineTo(21.0f, 10.0f)
                close()
                moveTo(7.0f, 15.0f)
                curveToRelative(-1.65f, 0.0f, -3.0f, -1.35f, -3.0f, -3.0f)
                curveToRelative(0.0f, -1.65f, 1.35f, -3.0f, 3.0f, -3.0f)
                reflectiveCurveToRelative(3.0f, 1.35f, 3.0f, 3.0f)
                curveTo(10.0f, 13.65f, 8.65f, 15.0f, 7.0f, 15.0f)
                close()
            }
        }
    }

    val Lightbulb: ImageVector by lazy {
        materialIcon(name = "App.Lightbulb") {
            materialPath {
                moveTo(9.0f, 21.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-1.0f)
                horizontalLineTo(9.0f)
                verticalLineToRelative(1.0f)
                close()
                moveTo(12.0f, 2.0f)
                curveTo(8.14f, 2.0f, 5.0f, 5.14f, 5.0f, 9.0f)
                curveToRelative(0.0f, 2.38f, 1.19f, 4.47f, 3.0f, 5.74f)
                verticalLineTo(17.0f)
                curveToRelative(0.0f, 0.55f, 0.45f, 1.0f, 1.0f, 1.0f)
                horizontalLineToRelative(6.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineToRelative(-2.26f)
                curveToRelative(1.81f, -1.27f, 3.0f, -3.36f, 3.0f, -5.74f)
                curveToRelative(0.0f, -3.86f, -3.14f, -7.0f, -7.0f, -7.0f)
                close()
            }
        }
    }

    val Link: ImageVector by lazy {
        materialIcon(name = "App.Link") {
            materialPath {
                moveTo(3.9f, 12.0f)
                curveToRelative(0.0f, -1.71f, 1.39f, -3.1f, 3.1f, -3.1f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(7.0f)
                horizontalLineTo(7.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, 2.24f, -5.0f, 5.0f)
                reflectiveCurveToRelative(2.24f, 5.0f, 5.0f, 5.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(-1.9f)
                horizontalLineTo(7.0f)
                curveToRelative(-1.71f, 0.0f, -3.1f, -1.39f, -3.1f, -3.1f)
                close()
                moveTo(8.0f, 13.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(8.0f)
                verticalLineTo(13.0f)
                close()
                moveTo(17.0f, 7.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(1.9f)
                horizontalLineToRelative(4.0f)
                curveToRelative(1.71f, 0.0f, 3.1f, 1.39f, 3.1f, 3.1f)
                reflectiveCurveToRelative(-1.39f, 3.1f, -3.1f, 3.1f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(17.0f)
                horizontalLineToRelative(4.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, -2.24f, 5.0f, -5.0f)
                reflectiveCurveToRelative(-2.24f, -5.0f, -5.0f, -5.0f)
                close()
            }
        }
    }

    val LocalFireDepartment: ImageVector by lazy {
        materialIcon(name = "App.LocalFireDepartment") {
            materialPath {
                moveTo(12.0f, 12.9f)
                lineToRelative(-2.13f, 2.09f)
                curveTo(9.31f, 15.55f, 9.0f, 16.28f, 9.0f, 17.06f)
                curveTo(9.0f, 18.68f, 10.35f, 20.0f, 12.0f, 20.0f)
                reflectiveCurveToRelative(3.0f, -1.32f, 3.0f, -2.94f)
                curveToRelative(0.0f, -0.78f, -0.31f, -1.52f, -0.87f, -2.07f)
                lineTo(12.0f, 12.9f)
                close()
                moveTo(16.0f, 6.0f)
                lineToRelative(-0.44f, 0.55f)
                curveTo(14.38f, 8.02f, 12.0f, 7.19f, 12.0f, 5.3f)
                verticalLineTo(2.0f)
                curveToRelative(0.0f, 0.0f, -8.0f, 4.0f, -8.0f, 11.0f)
                curveToRelative(0.0f, 2.92f, 1.56f, 5.47f, 3.89f, 6.86f)
                curveTo(7.33f, 19.07f, 7.0f, 18.1f, 7.0f, 17.06f)
                curveToRelative(0.0f, -1.32f, 0.52f, -2.56f, 1.47f, -3.5f)
                lineTo(12.0f, 10.1f)
                lineToRelative(3.53f, 3.47f)
                curveToRelative(0.95f, 0.93f, 1.47f, 2.17f, 1.47f, 3.5f)
                curveToRelative(0.0f, 1.02f, -0.31f, 1.96f, -0.85f, 2.75f)
                curveToRelative(1.89f, -1.15f, 3.29f, -3.06f, 3.71f, -5.3f)
                curveTo(20.52f, 10.97f, 18.79f, 7.62f, 16.0f, 6.0f)
                close()
            }
        }
    }

    val Psychology: ImageVector by lazy {
        materialIcon(name = "App.Psychology") {
            materialPath {
                moveTo(13.0f, 8.57f)
                curveToRelative(-0.79f, 0.0f, -1.43f, 0.64f, -1.43f, 1.43f)
                reflectiveCurveToRelative(0.64f, 1.43f, 1.43f, 1.43f)
                reflectiveCurveToRelative(1.43f, -0.64f, 1.43f, -1.43f)
                reflectiveCurveTo(13.79f, 8.57f, 13.0f, 8.57f)
                close()
                moveTo(13.0f, 3.0f)
                curveTo(9.25f, 3.0f, 6.2f, 5.94f, 6.02f, 9.64f)
                lineTo(4.1f, 12.2f)
                curveToRelative(-0.25f, 0.33f, -0.01f, 0.8f, 0.4f, 0.8f)
                horizontalLineTo(6.0f)
                verticalLineToRelative(3.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                horizontalLineToRelative(1.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(-4.68f)
                curveToRelative(2.36f, -1.12f, 4.0f, -3.53f, 4.0f, -6.32f)
                curveTo(20.0f, 6.13f, 16.87f, 3.0f, 13.0f, 3.0f)
                close()
            }
        }
    }

    val Public: ImageVector by lazy {
        materialIcon(name = "App.Public") {
            materialPath {
                moveTo(12.0f, 2.0f)
                curveTo(6.48f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.48f, 10.0f, 10.0f, 10.0f)
                reflectiveCurveToRelative(10.0f, -4.48f, 10.0f, -10.0f)
                reflectiveCurveTo(17.52f, 2.0f, 12.0f, 2.0f)
                close()
                moveTo(11.0f, 19.93f)
                curveToRelative(-3.95f, -0.49f, -7.0f, -3.85f, -7.0f, -7.93f)
                curveToRelative(0.0f, -0.62f, 0.08f, -1.21f, 0.21f, -1.79f)
                lineTo(9.0f, 15.0f)
                verticalLineToRelative(1.0f)
                curveToRelative(0.0f, 1.1f, 0.9f, 2.0f, 2.0f, 2.0f)
                verticalLineTo(19.93f)
                close()
                moveTo(17.9f, 17.39f)
                curveToRelative(-0.26f, -0.81f, -1.0f, -1.39f, -1.9f, -1.39f)
                horizontalLineToRelative(-1.0f)
                verticalLineToRelative(-3.0f)
                curveToRelative(0.0f, -0.55f, -0.45f, -1.0f, -1.0f, -1.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(0.55f, 0.0f, 1.0f, -0.45f, 1.0f, -1.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(2.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(4.59f)
                curveToRelative(2.93f, 1.19f, 5.0f, 4.06f, 5.0f, 7.41f)
                curveTo(20.0f, 14.08f, 19.2f, 15.97f, 17.9f, 17.39f)
                close()
            }
        }
    }

    val RestartAlt: ImageVector by lazy {
        materialIcon(name = "App.RestartAlt") {
            materialPath {
                moveTo(12.0f, 5.0f)
                verticalLineTo(2.0f)
                lineTo(8.0f, 6.0f)
                lineToRelative(4.0f, 4.0f)
                verticalLineTo(7.0f)
                curveToRelative(3.31f, 0.0f, 6.0f, 2.69f, 6.0f, 6.0f)
                curveToRelative(0.0f, 2.97f, -2.17f, 5.43f, -5.0f, 5.91f)
                verticalLineToRelative(2.02f)
                curveToRelative(3.95f, -0.49f, 7.0f, -3.85f, 7.0f, -7.93f)
                curveTo(20.0f, 8.58f, 16.42f, 5.0f, 12.0f, 5.0f)
                close()
                moveTo(6.0f, 13.0f)
                curveToRelative(0.0f, -1.65f, 0.67f, -3.15f, 1.76f, -4.24f)
                lineTo(6.34f, 7.34f)
                curveTo(4.9f, 8.79f, 4.0f, 10.79f, 4.0f, 13.0f)
                curveToRelative(0.0f, 4.08f, 3.05f, 7.44f, 7.0f, 7.93f)
                verticalLineToRelative(-2.02f)
                curveTo(8.17f, 18.43f, 6.0f, 15.97f, 6.0f, 13.0f)
                close()
            }
        }
    }

    val Schedule: ImageVector by lazy {
        materialIcon(name = "App.Schedule") {
            materialPath {
                moveTo(11.99f, 2.0f)
                curveTo(6.47f, 2.0f, 2.0f, 6.48f, 2.0f, 12.0f)
                reflectiveCurveToRelative(4.47f, 10.0f, 9.99f, 10.0f)
                curveTo(17.52f, 22.0f, 22.0f, 17.52f, 22.0f, 12.0f)
                reflectiveCurveTo(17.52f, 2.0f, 11.99f, 2.0f)
                close()
                moveTo(12.0f, 20.0f)
                curveToRelative(-4.42f, 0.0f, -8.0f, -3.58f, -8.0f, -8.0f)
                reflectiveCurveToRelative(3.58f, -8.0f, 8.0f, -8.0f)
                reflectiveCurveToRelative(8.0f, 3.58f, 8.0f, 8.0f)
                reflectiveCurveToRelative(-3.58f, 8.0f, -8.0f, 8.0f)
                close()
                moveTo(12.5f, 7.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(6.0f)
                lineToRelative(5.25f, 3.15f)
                lineToRelative(0.75f, -1.23f)
                lineToRelative(-4.5f, -2.67f)
                close()
            }
        }
    }

    val SevereCold: ImageVector by lazy {
        materialIcon(name = "App.SevereCold") {
            materialPath {
                moveTo(12.0f, 2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(3.17f)
                lineToRelative(2.59f, -2.59f)
                lineTo(18.0f, 4.0f)
                lineToRelative(-4.0f, 4.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(3.0f)
                lineToRelative(4.0f, -4.0f)
                lineToRelative(1.41f, 1.41f)
                lineTo(19.83f, 11.0f)
                horizontalLineTo(23.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(-3.17f)
                lineToRelative(2.59f, 2.59f)
                lineTo(21.0f, 17.0f)
                lineToRelative(-4.0f, -4.0f)
                horizontalLineToRelative(-3.0f)
                verticalLineToRelative(3.0f)
                lineToRelative(4.0f, 4.0f)
                lineToRelative(-1.41f, 1.41f)
                lineTo(14.0f, 18.83f)
                verticalLineTo(22.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(-3.17f)
                lineToRelative(-2.59f, 2.59f)
                lineTo(8.0f, 20.0f)
                lineToRelative(4.0f, -4.0f)
                verticalLineToRelative(-3.0f)
                horizontalLineTo(9.0f)
                lineToRelative(-4.0f, 4.0f)
                lineToRelative(-1.41f, -1.41f)
                lineTo(6.17f, 13.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(3.17f)
                lineTo(3.59f, 8.41f)
                lineTo(5.0f, 7.0f)
                lineToRelative(4.0f, 4.0f)
                horizontalLineToRelative(3.0f)
                verticalLineTo(8.0f)
                lineTo(8.0f, 4.0f)
                lineToRelative(1.41f, -1.41f)
                lineTo(12.0f, 5.17f)
                verticalLineTo(2.0f)
                close()
            }
        }
    }

    val Tune: ImageVector by lazy {
        materialIcon(name = "App.Tune") {
            materialPath {
                moveTo(3.0f, 17.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(3.0f, 5.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                verticalLineTo(5.0f)
                horizontalLineTo(3.0f)
                close()
                moveTo(13.0f, 21.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(2.0f)
                close()
                moveTo(7.0f, 9.0f)
                verticalLineToRelative(2.0f)
                horizontalLineTo(3.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(9.0f)
                horizontalLineTo(7.0f)
                close()
                moveTo(21.0f, 13.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineTo(11.0f)
                verticalLineToRelative(2.0f)
                horizontalLineToRelative(10.0f)
                close()
                moveTo(15.0f, 9.0f)
                horizontalLineToRelative(2.0f)
                verticalLineTo(7.0f)
                horizontalLineToRelative(4.0f)
                verticalLineTo(5.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(-2.0f)
                verticalLineToRelative(6.0f)
                close()
            }
        }
    }

    val UploadFile: ImageVector by lazy {
        materialIcon(name = "App.UploadFile") {
            materialPath {
                moveTo(14.0f, 2.0f)
                horizontalLineTo(6.0f)
                curveTo(4.9f, 2.0f, 4.01f, 2.9f, 4.01f, 4.0f)
                lineTo(4.0f, 20.0f)
                curveToRelative(0.0f, 1.1f, 0.89f, 2.0f, 1.99f, 2.0f)
                horizontalLineTo(18.0f)
                curveToRelative(1.1f, 0.0f, 2.0f, -0.9f, 2.0f, -2.0f)
                verticalLineTo(8.0f)
                lineTo(14.0f, 2.0f)
                close()
                moveTo(18.0f, 20.0f)
                horizontalLineTo(6.0f)
                verticalLineTo(4.0f)
                horizontalLineToRelative(7.0f)
                verticalLineToRelative(5.0f)
                horizontalLineToRelative(5.0f)
                verticalLineTo(20.0f)
                close()
                moveTo(8.0f, 15.01f)
                lineToRelative(1.41f, 1.41f)
                lineTo(11.0f, 14.84f)
                verticalLineTo(19.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(-4.16f)
                lineToRelative(1.59f, 1.59f)
                lineTo(16.0f, 15.01f)
                lineTo(12.01f, 11.0f)
                lineTo(8.0f, 15.01f)
                close()
            }
        }
    }

    val Verified: ImageVector by lazy {
        materialIcon(name = "App.Verified") {
            materialPath {
                moveTo(23.0f, 12.0f)
                lineToRelative(-2.44f, -2.79f)
                lineToRelative(0.34f, -3.69f)
                lineToRelative(-3.61f, -0.82f)
                lineTo(15.4f, 1.5f)
                lineTo(12.0f, 2.96f)
                lineTo(8.6f, 1.5f)
                lineTo(6.71f, 4.69f)
                lineTo(3.1f, 5.5f)
                lineTo(3.44f, 9.2f)
                lineTo(1.0f, 12.0f)
                lineToRelative(2.44f, 2.79f)
                lineToRelative(-0.34f, 3.7f)
                lineToRelative(3.61f, 0.82f)
                lineTo(8.6f, 22.5f)
                lineToRelative(3.4f, -1.47f)
                lineToRelative(3.4f, 1.46f)
                lineToRelative(1.89f, -3.19f)
                lineToRelative(3.61f, -0.82f)
                lineToRelative(-0.34f, -3.69f)
                lineTo(23.0f, 12.0f)
                close()
                moveTo(10.09f, 16.72f)
                lineToRelative(-3.8f, -3.81f)
                lineToRelative(1.48f, -1.48f)
                lineToRelative(2.32f, 2.33f)
                lineToRelative(5.85f, -5.87f)
                lineToRelative(1.48f, 1.48f)
                lineTo(10.09f, 16.72f)
                close()
            }
        }
    }

    val Visibility: ImageVector by lazy {
        materialIcon(name = "App.Visibility") {
            materialPath {
                moveTo(12.0f, 4.5f)
                curveTo(7.0f, 4.5f, 2.73f, 7.61f, 1.0f, 12.0f)
                curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                reflectiveCurveToRelative(9.27f, -3.11f, 11.0f, -7.5f)
                curveToRelative(-1.73f, -4.39f, -6.0f, -7.5f, -11.0f, -7.5f)
                close()
                moveTo(12.0f, 17.0f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                reflectiveCurveToRelative(2.24f, -5.0f, 5.0f, -5.0f)
                reflectiveCurveToRelative(5.0f, 2.24f, 5.0f, 5.0f)
                reflectiveCurveToRelative(-2.24f, 5.0f, -5.0f, 5.0f)
                close()
                moveTo(12.0f, 9.0f)
                curveToRelative(-1.66f, 0.0f, -3.0f, 1.34f, -3.0f, 3.0f)
                reflectiveCurveToRelative(1.34f, 3.0f, 3.0f, 3.0f)
                reflectiveCurveToRelative(3.0f, -1.34f, 3.0f, -3.0f)
                reflectiveCurveToRelative(-1.34f, -3.0f, -3.0f, -3.0f)
                close()
            }
        }
    }

    val VisibilityOff: ImageVector by lazy {
        materialIcon(name = "App.VisibilityOff") {
            materialPath {
                moveTo(12.0f, 7.0f)
                curveToRelative(2.76f, 0.0f, 5.0f, 2.24f, 5.0f, 5.0f)
                curveToRelative(0.0f, 0.65f, -0.13f, 1.26f, -0.36f, 1.83f)
                lineToRelative(2.92f, 2.92f)
                curveToRelative(1.51f, -1.26f, 2.7f, -2.89f, 3.43f, -4.75f)
                curveToRelative(-1.73f, -4.39f, -6.0f, -7.5f, -11.0f, -7.5f)
                curveToRelative(-1.4f, 0.0f, -2.74f, 0.25f, -3.98f, 0.7f)
                lineToRelative(2.16f, 2.16f)
                curveTo(10.74f, 7.13f, 11.35f, 7.0f, 12.0f, 7.0f)
                close()
                moveTo(2.0f, 4.27f)
                lineToRelative(2.28f, 2.28f)
                lineToRelative(0.46f, 0.46f)
                curveTo(3.08f, 8.3f, 1.78f, 10.02f, 1.0f, 12.0f)
                curveToRelative(1.73f, 4.39f, 6.0f, 7.5f, 11.0f, 7.5f)
                curveToRelative(1.55f, 0.0f, 3.03f, -0.3f, 4.38f, -0.84f)
                lineToRelative(0.42f, 0.42f)
                lineTo(19.73f, 22.0f)
                lineTo(21.0f, 20.73f)
                lineTo(3.27f, 3.0f)
                lineTo(2.0f, 4.27f)
                close()
                moveTo(7.53f, 9.8f)
                lineToRelative(1.55f, 1.55f)
                curveToRelative(-0.05f, 0.21f, -0.08f, 0.43f, -0.08f, 0.65f)
                curveToRelative(0.0f, 1.66f, 1.34f, 3.0f, 3.0f, 3.0f)
                curveToRelative(0.22f, 0.0f, 0.44f, -0.03f, 0.65f, -0.08f)
                lineToRelative(1.55f, 1.55f)
                curveToRelative(-0.67f, 0.33f, -1.41f, 0.53f, -2.2f, 0.53f)
                curveToRelative(-2.76f, 0.0f, -5.0f, -2.24f, -5.0f, -5.0f)
                curveToRelative(0.0f, -0.79f, 0.2f, -1.53f, 0.53f, -2.2f)
                close()
            }
        }
    }
}
