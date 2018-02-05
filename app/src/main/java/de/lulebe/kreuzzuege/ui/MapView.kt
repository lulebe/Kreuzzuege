package de.lulebe.kreuzzuege.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.util.TypedValue
import android.view.GestureDetector
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import de.lulebe.kreuzzuege.data.Faction
import de.lulebe.kreuzzuege.data.Game
import de.lulebe.kreuzzuege.data.Map
import de.lulebe.kreuzzuege.data.Unit
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread


class MapView : View {
    constructor(context: Context) : super(context) {
        init(null, 0)
    }
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs, 0)
    }
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        init(attrs, defStyle)
    }

    private var mGame: Game? = null
    private var mMap: Map? = null

    private val mBmpPaint = Paint()
    private val mReachableFieldPaint = Paint()
    private val mSelectedPaint = Paint()
    private val mUnitHealthBGPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val mUnitHealthPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var mBmpBackground: Bitmap? = null
    private var mSelected: Pair<Int, Int>? = null
    private val mBitmaps = GameBitmapStorage(context)
    private var mBitmapsReady = false
    private val mUnitHealthBGCornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2F, context.resources.displayMetrics)
    private val mSelectionCornerRadius = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4F, context.resources.displayMetrics)
    private var mFieldSize = 0F
    private var mIcSize = 0F
    private var mUnitHealthBGWidth = 0F
    private var mUnitHealthBGHeight = 0F
    private var mUnitHealthPadding = 0F
    private var mScrollX = 0F
        set(value) {
            mBmpBackground?.let { bmpBg ->
                var v = value
                if (value < 0) v = 0F
                val max = Math.max((bmpBg.width * mZoom) - width, 0F)
                if (value > max) v = max
                field = v
            }
        }
    private var mScrollY = 0F
        set(value) {
            mBmpBackground?.let { bmpBg ->
                var v = value
                if (value < 0) v = 0F
                val max = Math.max((bmpBg.height * mZoom) - height, 0F)
                if (value > max) v = max
                field = v
            }
        }
    private var mZoom = 1F
    private val mGestureDetector = GestureDetector(context, object: GestureDetector.OnGestureListener {
        override fun onShowPress(p0: MotionEvent?) {}
        override fun onDown(p0: MotionEvent?): Boolean {
            return true
        }
        override fun onFling(p0: MotionEvent?, p1: MotionEvent?, p2: Float, p3: Float): Boolean {
            return false
        }
        override fun onLongPress(p0: MotionEvent?) {}
        override fun onSingleTapUp(ev: MotionEvent?): Boolean {
            if (ev == null) return false
            val realX = ev.x * mZoom + mScrollX
            val realY = ev.y * mZoom + mScrollY
            val fieldX = realX.toInt() / mFieldSize.toInt()
            val fieldY = realY.toInt() / mFieldSize.toInt()
            mSelected = Pair(fieldX, fieldY)
            mGame?.selectedField = fieldY * mGame!!.map.sizeX + fieldX
            invalidate()
            performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
            return true
        }
        override fun onScroll(p0: MotionEvent?, p1: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
            mScrollX += distanceX
            mScrollY += distanceY
            invalidate()
            return true
        }
    })

    private val mGameChangeListener = {
        mGame?.let { game ->
            mSelected = if (game.selectedField != null)
                Pair(game.selectedField!! % game.map.sizeX, game.selectedField!! / game.map.sizeX)
            else
                null
            invalidate()
        }
        kotlin.Unit
    }

    init {
        mReachableFieldPaint.color = Color.parseColor("#66ff66")
        mReachableFieldPaint.style = Paint.Style.FILL
        mReachableFieldPaint.alpha = 80
        mSelectedPaint.color = Color.WHITE
        mSelectedPaint.style = Paint.Style.STROKE
        mSelectedPaint.strokeWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2F, context.resources.displayMetrics)
        mUnitHealthBGPaint.color = Color.BLACK
        mUnitHealthBGPaint.style = Paint.Style.FILL
        mUnitHealthPaint.color = Color.WHITE
    }

    private fun init(attrs: AttributeSet?, defStyle: Int) {

    }

    fun loadMap (map: Map) {
        mMap = map
        initBitmaps(width, height)
    }

    fun renderGame (game: Game) {
        mGame = game
        game.addListener(mGameChangeListener)
        if (mBitmapsReady)
            invalidate()
    }

    private fun initBitmaps (width: Int, height: Int) {
        mBitmapsReady = false
        mMap?.let { map ->
            val minFieldSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 50F, context.resources.displayMetrics)
            mFieldSize = Math.max(Math.min(width.toFloat() / map.sizeX, height.toFloat() / map.sizeY), minFieldSize)
            if (mFieldSize * map.sizeX < width)
                mFieldSize = width.toFloat() / map.sizeX
            mIcSize = mFieldSize / 4
            mUnitHealthPaint.textSize = mFieldSize / 5
            mUnitHealthBGWidth = mFieldSize / 4
            mUnitHealthBGHeight = mFieldSize / 5
            mUnitHealthPadding = mUnitHealthBGWidth / 12
            doAsync {
                mBitmaps.init(mFieldSize, mIcSize)
                mBmpBackground = mBitmaps.createMap(map)
                uiThread {
                    mBitmapsReady = true
                    invalidate()
                }
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        initBitmaps(w, h)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.parseColor("#decfac"))
        canvas.save()
        mBmpBackground?.let {
            canvas.translate(-mScrollX, -mScrollY)
            canvas.drawBitmap(it, 0F, 0F, mBmpPaint)
            drawGame(canvas)
            mGame?.selectedUnitMovementOptions!!.forEach {
                val x = it % mGame!!.map.sizeX
                val y = it / mGame!!.map.sizeX
                canvas.drawRect(
                        x * mFieldSize,
                        y * mFieldSize,
                        (x+1) * mFieldSize,
                        (y+1) * mFieldSize,
                        mReachableFieldPaint
                )
            }
            mSelected?.let {
                canvas.drawRoundRect(
                        it.first * mFieldSize,
                        it.second * mFieldSize,
                        it.first * mFieldSize + mFieldSize,
                        it.second * mFieldSize + mFieldSize,
                        mSelectionCornerRadius,
                        mSelectionCornerRadius,
                        mSelectedPaint)
            }
            canvas.restore()
        }
    }

    private fun drawGame (canvas: Canvas) {
        mGame?.let { game ->
            drawBuildingOwners(canvas, game)
            drawUnits(canvas, game)
        }
    }

    private fun drawBuildingOwners (canvas: Canvas, game: Game) {
        mBitmaps.icons[GameBitmapStorage.ICON_CRUSADERS]?.let { icon ->
            game.playerCrusaders.buildings.forEach {
                canvas.drawBitmap(
                        icon,
                        mFieldSize * (1+ (it % game.map.sizeX)) - mIcSize,
                        mFieldSize * (it / game.map.sizeX),
                        mBmpPaint
                )
            }
        }
        mBitmaps.icons[GameBitmapStorage.ICON_SARACEN]?.let { icon ->
            game.playerSaracen.buildings.forEach {
                canvas.drawBitmap(
                        icon,
                        mFieldSize * (1+ (it % game.map.sizeX)) - mIcSize,
                        mFieldSize * (it / game.map.sizeX),
                        mBmpPaint
                )
            }
        }
    }

    private fun drawUnits (canvas: Canvas, game: Game) {
        if (!mBitmapsReady) return
        game.playerCrusaders.units.forEach { unit ->
            drawUnit(canvas, unit, game)
        }
        game.playerSaracen.units.forEach { unit ->
            drawUnit(canvas, unit, game)
            canvas.drawBitmap(
                    mBitmaps.saracenUnits[unit.type],
                    mFieldSize * (unit.field % game.map.sizeX),
                    mFieldSize * (unit.field / game.map.sizeX),
                    mBmpPaint
            )
        }
    }

    private fun drawUnit (canvas: Canvas, unit: Unit, game: Game) {
        val unitBmp = if (unit.faction == Faction.CRUSADERS)
            mBitmaps.crusadersUnits[unit.type]
        else
            mBitmaps.saracenUnits[unit.type]
        canvas.drawBitmap(
                unitBmp,
                mFieldSize * (unit.field % game.map.sizeX),
                mFieldSize * (unit.field / game.map.sizeX),
                mBmpPaint
        )
        val healthBGRight = mFieldSize * (1 + (unit.field % game.map.sizeX)) - mSelectionCornerRadius/2
        val healthBGBottom = mFieldSize * (1 + unit.field / game.map.sizeX) - mSelectionCornerRadius/2
        canvas.drawRoundRect(
                healthBGRight - mUnitHealthBGWidth,
                healthBGBottom - mUnitHealthBGHeight,
                healthBGRight,
                healthBGBottom,
                mUnitHealthBGCornerRadius,
                mUnitHealthBGCornerRadius,
                mUnitHealthBGPaint
        )
        canvas.drawText(
                unit.hp.toString().padStart(2, '0'),
                healthBGRight - mUnitHealthBGWidth + mUnitHealthPadding,
                healthBGBottom - mUnitHealthPadding,
                mUnitHealthPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return mGestureDetector.onTouchEvent(event)
    }
}
