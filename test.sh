TEST_DIR="testcases/Test$1"
SOOT_DIR="sootOutput"

if [ -z "${1:-}" ]; then
    echo "Usage: $0 <test_number>"
    exit 1
fi

if [ ! -d "$TEST_DIR" ]; then
    echo "Error: $TEST_DIR not found"
    exit 1
fi

if [ ! -d "$SOOT_DIR" ]; then
    echo "Error: $SOOT_DIR not found"
    exit 1
fi

echo "Running Original Test..."
ORIG_MS=$(java -cp "$TEST_DIR" Test 2>/dev/null \
    | grep "Execution time" | awk '{print $3}' || echo "0")

sleep 30

echo "Running Optimized Test..."
OPT_MS=$(java -cp "$SOOT_DIR" Test 2>/dev/null \
    | grep "Execution time" | awk '{print $3}' || echo "0")

echo "Original Time:  ${ORIG_MS} ms"
echo "Optimized Time: ${OPT_MS} ms"

ABSOLUTE=$(echo "$ORIG_MS - $OPT_MS" | bc)
PERCENTAGE=$(echo "scale=2; ($ABSOLUTE / $ORIG_MS) * 100" | bc)

echo "---------------------------------"
echo "Absolute Improvement: $ABSOLUTE ms"
echo "Percentage Improvement: $PERCENTAGE%"